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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public UserSettingsDTO updateSettings(Long userId, UpdateSettingsRequest request) {
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
        // ... update other fields

        UserSettings updated = settingsRepository.save(settings);
        return convertToDTO(updated);
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
                .build();
    }
}

