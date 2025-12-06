package NytePulse.backend.service;

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
                .allowDirectMessages(true)
                .allowMentions(true)
                .allowTags(true)
                .allowStoriesMentions(true)
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
        try{
        UserSettings settings = settingsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Settings not found"));

        // Update settings
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

        UserSettings updated = settingsRepository.save(settings);
            Map<String, Object> response = new HashMap<>();
            response.put("message","Successfully updated settings");
            response.put("status",HttpStatus.OK.value());
            response.put("result",convertToDTO(updated));

        return ResponseEntity.ok(response);
        }catch (Exception e){
            log.error("Unexpected error update setting",
                    e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update setting");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private UserSettingsDTO convertToDTO(UserSettings settings) {
        return UserSettingsDTO.builder()
                .userId(settings.getUser().getUserId())
                .postVisibility(settings.getPostVisibility())
                .storyVisibility(settings.getStoryVisibility())
                .commentVisibility(settings.getCommentVisibility())
                .storyCommentVisibility(settings.getStoryCommentVisibility())
                .mentionVisibility(settings.getMentionVisibility())
                .tagVisibility(settings.getTagVisibility())
                .allowDirectMessages(settings.getAllowDirectMessages())
                .allowMentions(settings.getAllowMentions())
                .allowTags(settings.getAllowTags())
                .allowStoriesMentions(settings.getAllowStoriesMentions())
                .build();
    }
}

