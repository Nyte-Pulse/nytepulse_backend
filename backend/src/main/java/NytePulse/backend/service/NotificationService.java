package NytePulse.backend.service;

import NytePulse.backend.dto.NotificationDTO;
import NytePulse.backend.entity.Notification;
import NytePulse.backend.entity.User;
import NytePulse.backend.entity.UserSettings;
import NytePulse.backend.enums.NotificationType;
import NytePulse.backend.exception.ResourceNotFoundException;
import NytePulse.backend.repository.NotificationRepository;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;

    private final UserSettingsRepository userSettingsRepository;

    /**
     * Create and save a new notification with WebSocket push
     */
    @Transactional
    public Notification createNotification(
            Long recipientId,
            Long actorId,
            NotificationType type,
            String message,
            Long referenceId,
            String referenceType
    ) {
        try {
            System.out.println("Creating notification: type=" + type + ", recipientId=" + recipientId + ", actorId=" + actorId);
            User recipient = userRepository.findById(recipientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Recipient not found: " + recipientId));

            User actor = null;
            if (actorId != null) {
                actor = userRepository.findById(actorId)
                        .orElseThrow(() -> new ResourceNotFoundException("Actor not found: " + actorId));
            }

            if (actorId != null && recipientId.equals(actorId)) {
                log.debug("Skipping notification: actor and recipient are the same");
                return null;
            }

            System.out.println("Checking notification settings for user " + !shouldNotify(recipientId, type));

            if (!shouldNotify(recipientId, type)) {
                log.info("User {} has disabled notifications for type: {}", recipientId, type);
                return null;
            }

            Optional<Notification> existingNotification = notificationRepository
                    .findByRecipient_IdAndActor_IdAndTypeAndReferenceIdAndReferenceType(
                            recipientId, actorId, type, referenceId, referenceType
                    );

            if (existingNotification.isPresent()) {
                log.debug("Notification already exists, skipping creation");
                return existingNotification.get();
            }

            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .actor(actor)
                    .type(type)
                    .message(message)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .isRead(false)
                    .build();

            Notification saved = notificationRepository.save(notification);
            log.info("Notification created: type={}, recipient={}, actor={}", type, recipientId, actorId);

            NotificationDTO notificationDTO = convertToDTO(saved);
            webSocketService.sendNotificationToUser(recipientId, notificationDTO);

            Long unreadCount = getUnreadCount(recipientId);
            webSocketService.sendUnreadCountUpdate(recipientId, unreadCount);

            return saved;

        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage(), e);
            return null;
        }
    }

    private boolean shouldNotify(Long userId, NotificationType type) {
        try {
            Optional<UserSettings> settingsOpt = userSettingsRepository.findByUserId(userId);

            if (settingsOpt.isEmpty()) {
                log.debug("No settings found for user {}, allowing notification", userId);
                return true;
            }

            UserSettings settings = settingsOpt.get();

            switch (type) {
                case NEW_FOLLOWER:
                    return settings.getNotifyNewFollower() != null ? settings.getNotifyNewFollower() : true;

                case LIKE_POST:
                    return settings.getNotifyLikePost() != null ? settings.getNotifyLikePost() : true;

                case LIKE_COMMENT:
                    return settings.getNotifyLikeComment() != null ? settings.getNotifyLikeComment() : true;

                case COMMENT_POST:
                    return settings.getNotifyCommentPost() != null ? settings.getNotifyCommentPost() : true;

                case COMMENT_STORY:
                    return settings.getNotifyCommentStory() != null ? settings.getNotifyCommentStory() : true;

                case MENTION_POST:
                case MENTION_COMMENT:
                    return settings.getNotifyMention() != null ? settings.getNotifyMention() : true;

                case TAG_POST:
                    return settings.getNotifyTag() != null ? settings.getNotifyTag() : true;

                case SHARE_POST:
                    return settings.getNotifyShare() != null ? settings.getNotifyShare() : true;

                case FOLLOW_REQUEST:
                    return settings.getNotifyFollowRequest() != null ? settings.getNotifyFollowRequest() : true;

                case FOLLOW_REQUEST_ACCEPTED:
                    return settings.getNotifyFollowRequestAccepted() != null ? settings.getNotifyFollowRequestAccepted() : true;

                case NEW_MESSAGE:
                    return settings.getNotifyNewMessage() != null ? settings.getNotifyNewMessage() : true;

                default:
                    // For unknown types, default to true
                    return true;
            }

        } catch (Exception e) {
            log.error("Error checking notification settings for user {}: {}", userId, e.getMessage());
            return true;
        }
    }

    public Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByRecipient_IdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::convertToDTO);
    }

    public Page<NotificationDTO> getUnreadNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::convertToDTO);
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipient_IdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        // ✅ Send updated unread count via WebSocket
        Long unreadCount = getUnreadCount(userId);
        webSocketService.sendUnreadCountUpdate(userId, unreadCount);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByRecipientId(userId);

        // ✅ Send updated unread count (should be 0)
        webSocketService.sendUnreadCountUpdate(userId, 0L);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to notification");
        }

        notificationRepository.delete(notification);

        // ✅ Send updated unread count
        Long unreadCount = getUnreadCount(userId);
        webSocketService.sendUnreadCountUpdate(userId, unreadCount);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .actorId(notification.getActor() != null ? notification.getActor().getId() : null)
                .actorUsername(notification.getActor() != null ? notification.getActor().getUsername() : null)
                .actorProfilePicture(notification.getActor() != null ? getActorProfilePicture(notification.getActor()) : null)
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private String getActorProfilePicture(User actor) {
        // Implement logic to get profile picture
        return null;
    }
}
