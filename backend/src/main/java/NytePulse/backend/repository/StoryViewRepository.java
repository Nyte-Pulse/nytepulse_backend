package NytePulse.backend.repository;

import NytePulse.backend.entity.StoryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface StoryViewRepository extends JpaRepository<StoryView, Long> {

    List<StoryView> findByStoryId(Long storyId);
    boolean existsByStoryIdAndUserId(Long storyId, Long userId);

    Long countByStoryId(Long storyId);
    @Modifying
    @Transactional
    @Query("DELETE FROM StoryView s WHERE s.viewedAt < :cutoffTime")
    void deleteOldViews(LocalDateTime cutoffTime);
}