package NytePulse.backend.service;

import NytePulse.backend.dto.NotificationDTO;
import NytePulse.backend.entity.Notification;
import NytePulse.backend.entity.User;
import NytePulse.backend.service.centralServices.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    private final UserService userService;

    public static final Map<Long, String> ONLINE_USERS = new ConcurrentHashMap<>();

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


//    @EventListener
//    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
//        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//
//        Principal principal = headerAccessor.getUser();
//
//        if (principal != null) {
//            try {
//                String userIdString = principal.getName();
//                Long userId = Long.parseLong(userIdString);
//
//                ONLINE_USERS.put(userId, headerAccessor.getSessionId());
//
//                userService.updateUserStatus(userId, true);
//
//                broadcastStatusChange(userId, "ONLINE");
//
//                log.info("User Connected: ID={} Session={}", userId, headerAccessor.getSessionId());
//
//            } catch (NumberFormatException e) {
//                log.error("Failed to parse User ID from Principal: {}", principal.getName());
//            } catch (Exception e) {
//                log.error("Error in Connect Listener: {}", e.getMessage());
//            }
//        } else {
//            log.warn("Connection established but no Principal found (Guest?)");
//        }
//    }
//
//    @EventListener
//    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//        String sessionId = headerAccessor.getSessionId();
//
//        Long userId = getKeyByValue(ONLINE_USERS, sessionId);
//
//        if (userId != null) {
//            ONLINE_USERS.remove(userId);
//
//            try {
//                userService.updateUserStatus(userId, false);
//            } catch (Exception e) {
//                log.error("Failed to update user status on disconnect: {}", e.getMessage());
//            }
//
//            broadcastStatusChange(userId, "OFFLINE");
//            log.info("User Disconnected: ID={}", userId);
//        }
//    }
//    private void broadcastStatusChange(Long userId, String status) {
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("userId", userId);
//        payload.put("status", status);
//        payload.put("timestamp", LocalDateTime.now().toString()); // Use .toString() for easier JSON parsing in Flutter
//
//        messagingTemplate.convertAndSend("/topic/public/status", payload);
//    }
//
//    private static Long getKeyByValue(Map<Long, String> map, String value) {
//        for (Map.Entry<Long, String> entry : map.entrySet()) {
//            if (Objects.equals(value, entry.getValue())) {
//                return entry.getKey();
//            }
//        }
//        return null;
//    }

}
