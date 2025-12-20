package NytePulse.backend.repository;

import NytePulse.backend.entity.Post;
import NytePulse.backend.entity.User;
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
}
