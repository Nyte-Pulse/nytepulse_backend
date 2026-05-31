package NytePulse.backend.service.impl;
import NytePulse.backend.entity.Story;
import NytePulse.backend.entity.StoryLike;
import NytePulse.backend.entity.User;
import NytePulse.backend.enums.NotificationType;
import NytePulse.backend.repository.StoryLikeRepository;
import NytePulse.backend.repository.StoryRepository;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.service.FcmService;
import NytePulse.backend.service.NotificationService;
import NytePulse.backend.service.centralServices.StoryLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class StoryLikeServiceImpl implements StoryLikeService {

    @Autowired
    private  StoryLikeRepository storyLikeRepository;

    @Autowired
    private  FcmService fcmService;

    @Autowired
    private  NotificationService notificationService;

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private StoryRepository storyRepository;

    public StoryLikeServiceImpl(StoryLikeRepository storyLikeRepository) {
        this.storyLikeRepository = storyLikeRepository;
    }

    @Override
    @Transactional
    public ResponseEntity<?> toggleLike(Long storyId, Long userId) {
        try {
            Optional<StoryLike> existingLike = storyLikeRepository.findByStoryIdAndUserId(storyId, userId);
            Map<String, Object> response = new HashMap<>();

            Optional<Story> storyOpt = storyRepository.findById(storyId);

            if (existingLike.isPresent()) {
                         storyLikeRepository.delete(existingLike.get());

                response.put("liked", false);
                response.put("message", "Story like removed successfully");
            } else {
                        StoryLike newLike = new StoryLike();
                newLike.setStoryId(storyId);
                newLike.setUserId(userId);
                storyLikeRepository.save(newLike);

                response.put("liked", true);
                response.put("message", "Story liked successfully");
            }

                User following = userRepository.findById(storyOpt.get().getUser().getId()).orElseThrow(() -> new RuntimeException("User not found"));

            User currentUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Current user not found"));
            String messagePushNotification =following.getUsername() + " liked your story";

            String notifMsg =following.getUsername() + " liked your story.";
            notificationService.createNotification(
                    currentUser.getId(),                // Recipient
                    userId,                         // Sender
                    NotificationType.MENTION_POST,  // Enum (Ensure you have this or similar)
                    notifMsg,                       // Message
                    storyId,              // Reference ID
                    "POST"                          // Reference Type
            );

                String targetFcmToken = following.getFcmToken();

                if (targetFcmToken != null && !targetFcmToken.isEmpty()) {

                    fcmService.sendPushNotification(

                            targetFcmToken,

                            "New Like to Your Story!",

                            messagePushNotification,
                            Map.of("storyId", storyId.toString(), "type", "story_like")

                    );

                } else {

                    log.warn("No FCM token found for user: {}", following.getUserId());

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