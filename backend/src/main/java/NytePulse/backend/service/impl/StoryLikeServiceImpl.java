package NytePulse.backend.service.impl;
import NytePulse.backend.entity.StoryLike;
import NytePulse.backend.repository.StoryLikeRepository;
import NytePulse.backend.service.centralServices.StoryLikeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class StoryLikeServiceImpl implements StoryLikeService {

    private final StoryLikeRepository storyLikeRepository;

    public StoryLikeServiceImpl(StoryLikeRepository storyLikeRepository) {
        this.storyLikeRepository = storyLikeRepository;
    }

    @Override
    @Transactional
    public ResponseEntity<?> toggleLike(Long storyId, Long userId) {
        try {
            Optional<StoryLike> existingLike = storyLikeRepository.findByStoryIdAndUserId(storyId, userId);
            Map<String, Object> response = new HashMap<>();

            if (existingLike.isPresent()) {
                // If it exists, remove it (Toggle OFF)
                storyLikeRepository.delete(existingLike.get());

                response.put("liked", false);
                response.put("message", "Story like removed successfully");
            } else {
                // If it doesn't exist, create it (Toggle ON)
                StoryLike newLike = new StoryLike();
                newLike.setStoryId(storyId);
                newLike.setUserId(userId);
                storyLikeRepository.save(newLike);

                response.put("liked", true);
                response.put("message", "Story liked successfully");
            }

            response.put("status", HttpStatus.OK.value());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to toggle story like");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Runs every hour (3600000 milliseconds) to clean up old likes
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredLikes() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        storyLikeRepository.deleteLikesOlderThan(twentyFourHoursAgo);
        System.out.println("Cleaned up story likes older than: " + twentyFourHoursAgo);
    }
}