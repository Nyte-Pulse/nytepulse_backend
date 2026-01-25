package NytePulse.backend.repository;

import NytePulse.backend.entity.Post;
import NytePulse.backend.entity.SavedPost;
import NytePulse.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {

    // Check if already saved
    boolean existsByUserAndPost(User user, Post post);

    // Find specific saved entry (for unsaving)
    Optional<SavedPost> findByUserAndPost(User user, Post post);

    // Get all posts saved by a user (Paginated)
    Page<SavedPost> findByUserOrderBySavedAtDesc(User user, Pageable pageable);
}