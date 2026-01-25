package NytePulse.backend.service;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StreamCounterService {

    private final SimpMessagingTemplate messagingTemplate;

    // Stores: StreamKey -> Number of Viewers
    private final ConcurrentHashMap<String, AtomicInteger> streamViewers = new ConcurrentHashMap<>();

    // Stores: SessionID -> StreamKey (To know which stream to decrement on disconnect)
    private final ConcurrentHashMap<String, String> sessionStreamMap = new ConcurrentHashMap<>();

    public StreamCounterService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        String dest = (String) event.getMessage().getHeaders().get("simpDestination");
        // Destination looks like: /topic/streams/93a8...

        if (dest != null && dest.startsWith("/topic/streams/")) {
            String streamKey = dest.substring("/topic/streams/".length());
            String sessionId = (String) event.getMessage().getHeaders().get("simpSessionId");

            sessionStreamMap.put(sessionId, streamKey);
            int newCount = streamViewers.computeIfAbsent(streamKey, k -> new AtomicInteger(0)).incrementAndGet();

            // Broadcast new count to everyone in that stream
            broadcastCount(streamKey, newCount);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String streamKey = sessionStreamMap.remove(sessionId);

        if (streamKey != null) {
            int newCount = streamViewers.get(streamKey).decrementAndGet();
            if (newCount < 0) newCount = 0; // Safety

            broadcastCount(streamKey, newCount);
        }
    }

    private void broadcastCount(String streamKey, int count) {
        // Send JSON: {"count": 154}
        String json = "{\"count\": " + count + "}";
        messagingTemplate.convertAndSend("/topic/streams/" + streamKey, json);
    }
}