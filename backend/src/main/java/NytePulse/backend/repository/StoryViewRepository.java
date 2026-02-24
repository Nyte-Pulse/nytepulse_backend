package NytePulse.backend.repository;

import NytePulse.backend.entity.StoryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface StoryViewRepository extends JpaRepository<StoryView, Long> {

    List<StoryView> findByStoryId(Long storyId);
    boolean existsByStoryIdAndUserId(Long storyId, Long userId);

    Long countByStoryId(Long storyId);
    @Modifying
    @Transactional
    @Query("DELETE FROM StoryView s WHERE s.viewedAt < :cutoffTime")
    void deleteOldViews(LocalDateTime cutoffTime);

    @Query("SELECT sv.storyId FROM StoryView sv WHERE sv.userId = :userId AND sv.storyId IN :storyIds")
    Set<Long> findStoryIdsByUserIdAndStoryIdIn(@Param("userId") Long userId, @Param("storyIds") List<Long> storyIds);
}