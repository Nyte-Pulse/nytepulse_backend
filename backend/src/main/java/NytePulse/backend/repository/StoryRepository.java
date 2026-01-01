package NytePulse.backend.repository;

import NytePulse.backend.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findByExpiresAtBefore(LocalDateTime dateTime);
    List<Story> findByUserUserIdAndExpiresAtAfterOrderByCreatedAtDesc(String userId, LocalDateTime now);

    Optional<Story> findStoryById(Long storyId);

    List<Story> findByExpiresAtAfterOrderByCreatedAtDesc(LocalDateTime now);
}
