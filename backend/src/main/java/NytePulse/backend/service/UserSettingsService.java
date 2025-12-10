package NytePulse.backend.service;

import NytePulse.backend.dto.NotificationSettingsDTO;
import NytePulse.backend.dto.UpdateSettingsRequest;
import NytePulse.backend.dto.UserSettingsDTO;
import NytePulse.backend.entity.User;
import NytePulse.backend.entity.UserSettings;
import NytePulse.backend.enums.*;
import NytePulse.backend.exception.ResourceNotFoundException;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSettingsService {

    private final UserSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserSettingsDTO createDefaultSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        UserSettings settings = UserSettings.builder()
                .user(user)
                .postVisibility(PostVisibility.FOLLOWERS)
                .storyVisibility(StoryVisibility.FOLLOWERS)
                .commentVisibility(CommentVisibility.FOLLOWERS)
                .mentionVisibility(MentionVisibility.FOLLOWERS)
                .tagVisibility(TagVisibility.FOLLOWERS)
                .storyCommentVisibility(StoryCommentVisibility.FOLLOWERS)
                .allowDirectMessages(true)
                .allowMentions(true)
                .allowTags(true)
                .allowStoriesMentions(true)
                .notifyNewFollower(true)
                .notifyLikePost(true)
                .notifyLikeComment(true)
                .notifyCommentPost(true)
                .notifyCommentStory(true)
                .notifyMention(true)
                .notifyTag(true)
                .notifyShare(true)
                .notifyFollowRequest(true)
                .notifyFollowRequestAccepted(true)
                .build();

        UserSettings saved = settingsRepository.save(settings);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public UserSettingsDTO getSettings(Long userId) {
        UserSettings settings = settingsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Settings not found"));
        System.out.println("Retrieved settings: " + settings);
        return convertToDTO(settings);
    }

    @Transactional
    public ResponseEntity<?> updateSettings(Long userId, UpdateSettingsRequest request) {
        try {
            UserSettings settings = settingsRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Settings not found"));

            if (request.getPostVisibility() != null) {
                settings.setPostVisibility(request.getPostVisibility());
            }
            if (request.getStoryVisibility() != null) {
                settings.setStoryVisibility(request.getStoryVisibility());
            }
            if (request.getCommentVisibility() != null) {
                settings.setCommentVisibility(request.getCommentVisibility());
            }
            if (request.getStoryCommentVisibility() != null) {
                settings.setStoryCommentVisibility(request.getStoryCommentVisibility());
            }
            if (request.getMentionVisibility() != null) {
                settings.setMentionVisibility(request.getMentionVisibility());
            }
            if (request.getTagVisibility() != null) {
                settings.setTagVisibility(request.getTagVisibility());
            }

            if (request.getAllowMentions() != null) {
                settings.setAllowMentions(request.getAllowMentions());
            }
            if (request.getAllowTags() != null) {
                settings.setAllowTags(request.getAllowTags());
            }
            if (request.getAllowDirectMessages() != null) {
                settings.setAllowDirectMessages(request.getAllowDirectMessages());
            }
            if (request.getAllowStoriesMentions() != null) {
                settings.setAllowStoriesMentions(request.getAllowStoriesMentions());
            }

            if (request.getNotifyNewFollower() != null) {
                settings.setNotifyNewFollower(request.getNotifyNewFollower());
                log.debug("Updated notifyNewFollower to: {}", request.getNotifyNewFollower());
            }
            if (request.getNotifyLikePost() != null) {
                settings.setNotifyLikePost(request.getNotifyLikePost());
                log.debug("Updated notifyLikePost to: {}", request.getNotifyLikePost());
            }
            if (request.getNotifyLikeComment() != null) {
                settings.setNotifyLikeComment(request.getNotifyLikeComment());
                log.debug("Updated notifyLikeComment to: {}", request.getNotifyLikeComment());
            }
            if (request.getNotifyCommentPost() != null) {
                settings.setNotifyCommentPost(request.getNotifyCommentPost());
                log.debug("Updated notifyCommentPost to: {}", request.getNotifyCommentPost());
            }
            if (request.getNotifyCommentStory() != null) {
                settings.setNotifyCommentStory(request.getNotifyCommentStory());
                log.debug("Updated notifyCommentStory to: {}", request.getNotifyCommentStory());
            }
            if (request.getNotifyMention() != null) {
                settings.setNotifyMention(request.getNotifyMention());
                log.debug("Updated notifyMention to: {}", request.getNotifyMention());
            }
            if (request.getNotifyTag() != null) {
                settings.setNotifyTag(request.getNotifyTag());
                log.debug("Updated notifyTag to: {}", request.getNotifyTag());
            }
            if (request.getNotifyShare() != null) {
                settings.setNotifyShare(request.getNotifyShare());
                log.debug("Updated notifyShare to: {}", request.getNotifyShare());
            }
            if (request.getNotifyFollowRequest() != null) {
                settings.setNotifyFollowRequest(request.getNotifyFollowRequest());
                log.debug("Updated notifyFollowRequest to: {}", request.getNotifyFollowRequest());
            }
            if (request.getNotifyFollowRequestAccepted() != null) {
                settings.setNotifyFollowRequestAccepted(request.getNotifyFollowRequestAccepted());
                log.debug("Updated notifyFollowRequestAccepted to: {}", request.getNotifyFollowRequestAccepted());
            }

            UserSettings updated = settingsRepository.save(settings);
            log.info("Settings updated successfully for user ID: {}", userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully updated settings");
            response.put("status", HttpStatus.OK.value());
            response.put("result", convertToDTO(updated));

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            log.error("Settings not found for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Settings not found");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Unexpected error updating settings for user {}: {}", userId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update settings");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    private UserSettingsDTO convertToDTO(UserSettings settings) {
        return UserSettingsDTO.builder()
                .userId(settings.getUser().getUserId())
                .postVisibility(settings.getPostVisibility())
                .storyVisibility(settings.getStoryVisibility())
                .commentVisibility(settings.getCommentVisibility())
                .mentionVisibility(settings.getMentionVisibility())
                .tagVisibility(settings.getTagVisibility())
                .allowDirectMessages(settings.getAllowDirectMessages())
                .allowMentions(settings.getAllowMentions())
                .allowTags(settings.getAllowTags())
                .allowStoriesMentions(settings.getAllowStoriesMentions())
                .notifyNewFollower(settings.getNotifyNewFollower())
                .notifyLikePost(settings.getNotifyLikePost())
                .notifyLikeComment(settings.getNotifyLikeComment())
                .notifyCommentPost(settings.getNotifyCommentPost())
                .notifyCommentStory(settings.getNotifyCommentStory())
                .notifyMention(settings.getNotifyMention())
                .notifyTag(settings.getNotifyTag())
                .notifyShare(settings.getNotifyShare())
                .notifyFollowRequest(settings.getNotifyFollowRequest())
                .notifyFollowRequestAccepted(settings.getNotifyFollowRequestAccepted())
                .build();
    }

}

