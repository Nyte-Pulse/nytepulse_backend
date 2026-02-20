package NytePulse.backend.repository;

import NytePulse.backend.entity.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {

    Optional<StoryLike> findByStoryIdAndUserId(Long storyId, Long userId);

    @Modifying
    @Query("DELETE FROM StoryLike s WHERE s.createdAt < :cutoffTime")
    void deleteLikesOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    List<StoryLike> findByStoryId(Long storyId);
}