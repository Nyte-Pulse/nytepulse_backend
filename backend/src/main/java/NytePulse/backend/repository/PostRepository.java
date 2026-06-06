package NytePulse.backend.repository;

import NytePulse.backend.entity.Post;
import NytePulse.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Post> findByUserOrderByCreatedAtDesc(User user);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.shareCount = p.shareCount + 1 WHERE p.id = :postId")
    void incrementShareCount(@Param("postId") Long postId);

    List<Post> findByTagFriendIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT r.following.id FROM UserRelationship r WHERE r.follower.id = :viewerId")
    List<Long> findFollowingIds(@Param("viewerId") Long viewerId);

    // 2. YOUR EXISTING SMART FEED (For users with friends)
    // Keep this exactly as is
//    @Query("SELECT DISTINCT p FROM Post p " +
//            "LEFT JOIN UserRelationship ur ON ur.following = p.user " +
//            "LEFT JOIN PostLike pl ON pl.post = p AND pl.user.id IN :followingIds " +
//            "LEFT JOIN Comment c ON c.post = p AND c.user.id IN :followingIds " +
//            "WHERE p.user.id IN :followingIds OR p.user.id = :viewerId " +
//            "GROUP BY p.id " +
//            "ORDER BY " +
//            " (CASE WHEN p.createdAt >= :latestTime THEN 1 ELSE 0 END) DESC, " +
//            " (COUNT(DISTINCT c.id) * 5 + COUNT(DISTINCT pl.id) * 2 + (COUNT(DISTINCT ur.id) * 0.01)) DESC, " +
//            " p.createdAt DESC")
//    Page<Post> findSmartFeed(
//            @Param("followingIds") List<Long> followingIds,
//            @Param("viewerId") Long viewerId,
//            @Param("latestTime") LocalDateTime latestTime,
//            Pageable pageable
//    );
//
//    // 3. NEW: GLOBAL DISCOVERY FEED (For new users with 0 friends)
//    // Uses the SAME 3-second rule, but orders by TOTAL likes/comments
//    @Query("SELECT p FROM Post p " +
//            "LEFT JOIN p.likes pl " +
//            "LEFT JOIN p.comments c " +
//            "GROUP BY p.id " +
//            "ORDER BY " +
//            " (CASE WHEN p.createdAt >= :latestTime THEN 1 ELSE 0 END) DESC, " + // 3-Sec Rule
//            " (SIZE(p.likes) + SIZE(p.comments)) DESC, " + // Global Popularity
//            " p.createdAt DESC")
//    Page<Post> findGlobalDiscoveryFeed(
//            @Param("latestTime") LocalDateTime latestTime,
//            Pageable pageable
//    );

    // 1. SMART FEED
//    @Query("SELECT DISTINCT p FROM Post p " +
//            "LEFT JOIN UserRelationship ur ON ur.following = p.user " +
//            "LEFT JOIN PostLike pl ON pl.post = p AND pl.user.id IN :followingIds " +
//            "LEFT JOIN Comment c ON c.post = p AND c.user.id IN :followingIds " +
//            "WHERE p.user.id IN :followingIds OR p.user.id = :viewerId " +
//            "GROUP BY p.id " +
//            "ORDER BY " +
//            " ( (COUNT(DISTINCT c.id) * 5) + (COUNT(DISTINCT pl.id) * 2) + (COUNT(DISTINCT ur.id) * 0.01) + " +
//            "   (CASE WHEN p.createdAt >= :latestTime THEN 20 ELSE 0 END) ) DESC, " +
//            " p.createdAt DESC")
//    Page<Post> findSmartFeed(
//            @Param("followingIds") List<Long> followingIds,
//            @Param("viewerId") Long viewerId,
//            @Param("latestTime") LocalDateTime latestTime,
//            Pageable pageable
//    );
//
//    // 2. GLOBAL DISCOVERY FEED
//    @Query("SELECT p FROM Post p " +
//            "LEFT JOIN p.likes pl " +
//            "LEFT JOIN p.comments c " +
//            "GROUP BY p.id " +
//            "ORDER BY " +
//            " ( (SIZE(p.likes) * 2) + (SIZE(p.comments) * 5) + " +
//            "   (CASE WHEN p.createdAt >= :latestTime THEN 20 ELSE 0 END) ) DESC, " +
//            " p.createdAt DESC")
//    Page<Post> findGlobalDiscoveryFeed(
//            @Param("latestTime") LocalDateTime latestTime,
//            Pageable pageable
//    );
    // 1. SMART FEED
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN UserRelationship ur ON ur.following = p.user " +
            "WHERE p.user.id = :viewerId OR p.user.id IN (SELECT ur2.following.id FROM UserRelationship ur2 WHERE ur2.follower.id = :viewerId) " +
            "GROUP BY p.id " +
            "ORDER BY " +
            " ( (COUNT(DISTINCT ur.id) * 0.01) + " +
            "   (CASE WHEN p.createdAt >= :latestTime THEN 20 ELSE 0 END) ) DESC, " +
            " p.createdAt DESC")
    Page<Post> findSmartFeed(
            @Param("viewerId") Long viewerId,
            @Param("latestTime") LocalDateTime latestTime,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Post p " +
            // This line removes posts the viewer has already liked
            "WHERE NOT EXISTS (SELECT 1 FROM PostLike pl WHERE pl.post = p AND pl.user.id = :viewerId) " +
            "ORDER BY " +
            " ( " +
            "   (SIZE(p.comments) * 3) + " +
            "   (SIZE(p.likes) * 1) + " +
            "   (CASE WHEN p.user.id IN (" +
            "       SELECT ur.following.id FROM UserRelationship ur WHERE ur.follower.id = :viewerId" +
            "   ) THEN 200 ELSE 0 END) + " +          // Massive boost if following the author
            "   (CASE WHEN p.createdAt >= :last24h THEN 100 ELSE 0 END) " +
            " ) DESC, " +
            " p.createdAt DESC")
    Page<Post> findPersonalizedSmartFeed(
            @Param("viewerId") Long viewerId,
            @Param("last24h") LocalDateTime last24h,
            Pageable pageable
    );

    @Query("SELECT p FROM Post p " +
            "ORDER BY " +
            " ( " +
            // 1. NETWORK TIER: Huge boost if you follow the author
            "   (CASE WHEN p.user.id IN (" +
            "       SELECT ur.following.id FROM UserRelationship ur WHERE ur.follower.id = :viewerId" +
            "   ) THEN 1500 ELSE 0 END) + " +

            // 2. RECENCY TIER: Time-decay buckets.
            // Gives brand new posts with 0 likes a chance to be seen!
            "   (CASE " +
            "      WHEN p.createdAt >= :last1Hour THEN 1000 " +   // Super fresh
            "      WHEN p.createdAt >= :last12Hours THEN 500 " +  // Today
            "      WHEN p.createdAt >= :last24Hours THEN 200 " +  // Yesterday
            "      WHEN p.createdAt >= :last3Days THEN 50 " +     // Recent
            "      ELSE 0 " +
            "    END) + " +

            // 3. ENGAGEMENT TIER: Rewards virality.
            // Allows highly liked posts from strangers to overtake regular posts.
            "   (SIZE(p.comments) * 5) + " +  // Comments are high-effort, worth more
            "   (SIZE(p.likes) * 2) " +       // Likes are low-effort

            " ) DESC, " +
            // Tie-breaker: If scores are exactly the same, show the newest one
            " p.createdAt DESC")
    Page<Post> findAdvancedSmartFeed(
            @Param("viewerId") Long viewerId,
            @Param("last1Hour") LocalDateTime last1Hour,
            @Param("last12Hours") LocalDateTime last12Hours,
            @Param("last24Hours") LocalDateTime last24Hours,
            @Param("last3Days") LocalDateTime last3Days,
            Pageable pageable
    );



    // 2. GLOBAL DISCOVERY FEED
    @Query("SELECT p FROM Post p " +
            "ORDER BY " +
            " (CASE WHEN p.createdAt >= :latestTime THEN 20 ELSE 0 END) DESC, " +
            " p.createdAt DESC")
    Page<Post> findGlobalDiscoveryFeed(
            @Param("latestTime") LocalDateTime latestTime,
            Pageable pageable
    );
    Page<Post> findByUserIdIn(List<Long> userIds, Pageable pageable);

    List<Post> findByIdIn(List<Long> ids);

//    @Query("SELECT DISTINCT p FROM Post p " +
//            "LEFT JOIN FETCH p.media " +
//            "WHERE p.id IN :postIds " +
//            "ORDER BY p.createdAt DESC")
//    List<Post> findPostsWithMediaByIds(@Param("postIds") List<Long> postIds);

    List<Post> findByIdInOrderByCreatedAtDesc(List<Long> postIds);
}
