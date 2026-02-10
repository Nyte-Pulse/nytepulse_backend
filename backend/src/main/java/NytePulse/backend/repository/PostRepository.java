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

    @Query("SELECT DISTINCT p FROM Post p " + // Added DISTINCT
            "LEFT JOIN UserRelationship ur ON ur.following = p.user " +
            "LEFT JOIN PostLike pl ON pl.post = p AND pl.user.id IN :followingIds " +
            "LEFT JOIN Comment c ON c.post = p AND c.user.id IN :followingIds " +
            "WHERE p.user.id IN :followingIds OR p.user.id = :viewerId " +
            "GROUP BY p.id " + // Group by ID to collapse duplicates from Joins
            "ORDER BY " +
            // 1. FRESHNESS: 3 Second Rule
            " (CASE WHEN p.createdAt >= :latestTime THEN 1 ELSE 0 END) DESC, " +
            // 2. ALGORITHM: Engagement Score
            " (COUNT(DISTINCT c.id) * 5 + COUNT(DISTINCT pl.id) * 2 + (COUNT(DISTINCT ur.id) * 0.01)) DESC, " +
            // 3. STABILITY: Tie-Breaker (Crucial for preventing duplicates across pages)
            " p.createdAt DESC")
    Page<Post> findSmartFeed(
            @Param("followingIds") List<Long> followingIds,
            @Param("viewerId") Long viewerId,
            @Param("latestTime") LocalDateTime latestTime,
            Pageable pageable
    );

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN p.likes pl " +
            "LEFT JOIN p.comments c " +
            // No WHERE clause restricts users -> Shows everyone's posts
            "GROUP BY p.id " +
            "ORDER BY " +
            " (CASE WHEN p.createdAt >= :latestTime THEN 1 ELSE 0 END) DESC, " + // 3-Sec Rule
            " (SIZE(p.likes) + SIZE(p.comments)) DESC, " + // Global Popularity
            " p.createdAt DESC")
    Page<Post> findGlobalDiscoveryFeed(
            @Param("latestTime") LocalDateTime latestTime,
            Pageable pageable
    );

    // Helper to get IDs of people the viewer follows
    @Query("SELECT r.following.id FROM UserRelationship r WHERE r.follower.id = :viewerId")
    List<Long> findFollowingIds(@Param("viewerId") Long viewerId);
}
