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

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN UserRelationship ur ON ur.following = p.user " +
            "LEFT JOIN PostLike pl ON pl.post = p AND pl.user.id IN :followingIds " +
            "LEFT JOIN Comment c ON c.post = p AND c.user.id IN :followingIds " +
            "WHERE p.user.id IN :followingIds OR p.user.id = :viewerId " +
            "GROUP BY p " +
            "ORDER BY " +
            // CHANGED: Checks if post is newer than the 'latestTime' passed from Controller
            " (CASE WHEN p.createdAt >= :latestTime THEN 1 ELSE 0 END) DESC, " +
            " (COUNT(DISTINCT c.id) * 5 + COUNT(DISTINCT pl.id) * 2 + (COUNT(DISTINCT ur.id) * 0.01)) DESC")
    Page<Post> findSmartFeed(
            @Param("followingIds") List<Long> followingIds,
            @Param("viewerId") Long viewerId,
            @Param("latestTime") LocalDateTime latestTime,
            Pageable pageable
    );

    // Helper to get IDs of people the viewer follows
    @Query("SELECT r.following.id FROM UserRelationship r WHERE r.follower.id = :viewerId")
    List<Long> findFollowingIds(@Param("viewerId") Long viewerId);
}
