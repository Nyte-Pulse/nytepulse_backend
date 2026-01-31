package NytePulse.backend.config;

import NytePulse.backend.service.WebSocketService; // Adjust import
import NytePulse.backend.service.centralServices.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserService userService;
    private final WebSocketService webSocketService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();

        if (user != null) {
            String userId = user.getName(); // In your AuthInterceptor, you likely set this as the User ID
            log.info("User Connected: {}", userId);

            // 1. Update DB to Online
            userService.setUserOnlineStatus(Long.parseLong(userId), true);

            // 2. Broadcast to others that this user is now Online
            webSocketService.broadcastUserStatus(Long.parseLong(userId), true);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();

        if (user != null) {
            String userId = user.getName();
            log.info("User Disconnected: {}", userId);

            // 1. Update DB to Offline
            userService.setUserOnlineStatus(Long.parseLong(userId), false);

            // 2. Broadcast to others that this user is now Offline
            webSocketService.broadcastUserStatus(Long.parseLong(userId), false);
        }
    }
}
