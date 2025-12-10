package NytePulse.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private  WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory broker for topic and queue
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages FROM client TO server
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint accessible at /ws
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new CustomHandshakeHandler())
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Enable SockJS fallback options

        registry.addEndpoint("/ws")
                .setHandshakeHandler(new CustomHandshakeHandler()) // ✅ Add custom handler
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
