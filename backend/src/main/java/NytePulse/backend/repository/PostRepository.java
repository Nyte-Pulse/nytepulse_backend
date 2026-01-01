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

    @Query("""
        SELECT DISTINCT p FROM Post p
        LEFT JOIN UserSettings us ON us.user.id = p.user.id
        LEFT JOIN UserRelationship ur ON ur.following.id = p.user.id AND ur.follower.id = :viewerId
        WHERE us.postVisibility = 'EVERYONE'
           OR (us.postVisibility = 'FOLLOWERS' AND ur.id IS NOT NULL)
           OR (p.user.id = :viewerId)
           OR (us IS NULL AND ur.id IS NOT NULL)
    """)
    Page<Post> findVisiblePostsForUser(@Param("viewerId") Long viewerId, Pageable pageable);
}
