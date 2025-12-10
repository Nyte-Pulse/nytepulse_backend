package NytePulse.backend.service;

import NytePulse.backend.dto.NotificationDTO;
import NytePulse.backend.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotificationToUser(Long userId, NotificationDTO notification) {
        try {
            log.info("Sending notification to user {}: {}", userId, notification.getType());

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notification
            );

            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + userId,
                    notification
            );

            log.info("Notification sent successfully");
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
        }
    }


    public void broadcastNotification(NotificationDTO notification) {
        try {
            log.info("Broadcasting notification: {}", notification.getType());
            messagingTemplate.convertAndSend("/topic/notifications", notification);
            log.info("Notification broadcast successfully");
        } catch (Exception e) {
            log.error("Error broadcasting notification: {}", e.getMessage(), e);
        }
    }


    public void sendUnreadCountUpdate(Long userId, Long unreadCount) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/unread-count",
                    unreadCount
            );
            log.info("Unread count {} sent to user {}", unreadCount, userId);
        } catch (Exception e) {
            log.error("Error sending unread count to user {}: {}", userId, e.getMessage(), e);
        }
    }
}
