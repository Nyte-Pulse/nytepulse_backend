package NytePulse.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private  JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Get token from header
            String authHeader = accessor.getFirstNativeHeader("Authorization");


            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    // Validate token
                    if (jwtTokenProvider.validateToken(token)) {
                        // Get user ID from token
                        Long userId = jwtTokenProvider.getUserIdFromJWT(token);

                        // Set user in accessor
                        accessor.setUser(new UsernamePasswordAuthenticationToken(
                                userId.toString(),
                                null,
                                new ArrayList<>()
                        ));
                    } else {
                        System.out.println("❌ Invalid token");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Token validation error: " + e.getMessage());
                }
            } else {
                System.out.println("❌ No Authorization header");
            }
        }

        return message;
    }
}
