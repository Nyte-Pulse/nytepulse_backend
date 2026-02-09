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
            // Join to calculate Author's total followers (Popularity)
            "LEFT JOIN UserRelationship ur_auth ON ur_auth.following = p.user " +
            // Join to check if Friends (people viewer follows) Liked the post
            "LEFT JOIN PostLike pl ON pl.post = p AND pl.user.id IN :followingIds " +
            // Join to check if Friends Commented on the post
            "LEFT JOIN Comment c ON c.post = p AND c.user.id IN :followingIds " +
            // Filter: Only show posts from people the viewer follows (Standard Feed)
            // OR remove this WHERE clause if you want a global "Discovery" feed
            "WHERE p.user.id IN :followingIds " +
            "GROUP BY p " +
            "ORDER BY " +
            // THE ALGORITHM:
            // (Friend Comments * 5) + (Friend Likes * 2) + (Author Followers * 0.01)
            " (COUNT(DISTINCT c.id) * 5 + COUNT(DISTINCT pl.id) * 2 + (COUNT(DISTINCT ur_auth.id) * 0.01)) DESC, " +
            " p.createdAt DESC")
    Page<Post> findPersonalizedFeed(@Param("followingIds") List<Long> followingIds, Pageable pageable);

    // Helper to get IDs of people the viewer follows
    @Query("SELECT r.following.id FROM UserRelationship r WHERE r.follower.id = :viewerId")
    List<Long> findFollowingIds(@Param("viewerId") Long viewerId);
}
