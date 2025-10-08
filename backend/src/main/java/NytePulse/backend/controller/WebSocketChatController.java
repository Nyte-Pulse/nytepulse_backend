package NytePulse.backend.controller;

import NytePulse.backend.dto.*;
import NytePulse.backend.entity.ChatMessage;
import NytePulse.backend.entity.MessageStatus;
import NytePulse.backend.repository.ChatMessageRepository;
import NytePulse.backend.repository.MessageStatusRepository;
import NytePulse.backend.service.centralServices.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Controller
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WebSocketChatController {

    @Autowired
    private  SimpMessagingTemplate messagingTemplate;

    @Autowired
    private  ChatService chatService;

    @Autowired
    private MessageStatusRepository messageStatusRepository;;

    @Autowired
    private ChatMessageRepository chatMessageRepository;


    /**
     * Send message to conversation
     * Client sends to: /app/chat.send/{conversationId}
     * Message broadcasted to: /topic/conversation/{conversationId}
     */
    @MessageMapping("/chat.send/{conversationId}")
    public void sendMessage(
            @DestinationVariable Long conversationId,
            @Payload Map<String, String> payload,
            Principal principal) {

        try {
            Long senderId = Long.parseLong(principal.getName());
            String content = payload.get("content");
            String messageType = payload.getOrDefault("messageType", "TEXT");
            String fileUrl = payload.get("fileUrl");

            // Save message to database
            ChatMessageDTO savedMessage = chatService.sendMessage(
                    conversationId,
                    senderId,
                    content,
                    messageType,
                    fileUrl
            );

            // Broadcast to all participants in conversation
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversationId,
                    savedMessage
            );

            // Send notification to participants
            ChatNotificationDTO notification = new ChatNotificationDTO();
            notification.setConversationId(conversationId);
            notification.setSenderId(senderId);
            notification.setSenderName(savedMessage.getSenderName());
            notification.setSenderAvatar(savedMessage.getSenderAvatar());
            notification.setMessage(content);
            notification.setTimestamp(LocalDateTime.now());
            notification.setType("NEW_MESSAGE");

            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + conversationId,
                    notification
            );

        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Typing indicator
     * Client sends to: /app/chat.typing/{conversationId}
     */
    @MessageMapping("/chat.typing/{conversationId}")
    public void userTyping(
            @DestinationVariable Long conversationId,
            @Payload Map<String, Boolean> payload,
            Principal principal) {

        try {
            Long userId = Long.parseLong(principal.getName());
            Boolean isTyping = payload.get("isTyping");

            // Using all-args constructor instead of setters
            TypingIndicatorDTO indicator = new TypingIndicatorDTO();
            indicator.setConversationId(conversationId);
            indicator.setUserId(userId);
            indicator.setTyping(isTyping);

            // Broadcast typing status to conversation
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversationId + "/typing",
                    indicator
            );

        } catch (Exception e) {
            System.err.println("Error sending typing indicator: " + e.getMessage());
        }
    }

    /**
     * User connected
     */
    @MessageMapping("/user.connect")
    public void userConnected(Principal principal) {
        try {
            Long userId = Long.parseLong(principal.getName());
            System.out.println("User connected: " + userId);

            // Broadcast online status
            messagingTemplate.convertAndSend(
                    "/topic/user/" + userId + "/status",
                    Map.of("userId", userId, "status", "ONLINE")
            );

        } catch (Exception e) {
            System.err.println("Error in user connect: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.delivered/{messageId}")
    public void markMessageDelivered(
            @DestinationVariable Long messageId,
            Principal principal) {

        try {
            System.out.println("=== DELIVERED HANDLER CALLED ===");
            System.out.println("Message ID: " + messageId);
            System.out.println("Principal: " + principal.getName());

            Long userId = Long.parseLong(principal.getName());

            Optional<MessageStatus> statusOpt = messageStatusRepository
                    .findByMessageIdAndUserId(messageId, userId);

            if (statusOpt.isPresent()) {
                MessageStatus status = statusOpt.get();
                status.setStatus(MessageStatus.Status.DELIVERED);
                status.setDeliveredAt(LocalDateTime.now());
                messageStatusRepository.save(status);

                System.out.println("✅ Updated status to DELIVERED");
                System.out.println("Delivered at: " + status.getDeliveredAt());
            } else {
                System.out.println("❌ MessageStatus not found for messageId=" + messageId + ", userId=" + userId);
            }

        } catch (Exception e) {
            System.err.println("❌ Error in handleDelivered: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @MessageMapping("/chat.read/{conversationId}")
    public void handleRead(
            @DestinationVariable Long conversationId,
            Principal principal) {

        try {
            System.out.println("=== READ HANDLER CALLED ===");
            System.out.println("Conversation ID: " + conversationId);
            System.out.println("Principal: " + principal.getName());

            Long userId = Long.parseLong(principal.getName());

            // Get all messages in conversation
            java.util.List<ChatMessage> messages = chatMessageRepository
                    .findByConversationIdOrderByCreatedAtDesc(conversationId);

            System.out.println("Found " + messages.size() + " messages in conversation");

            int updatedCount = 0;
            for (ChatMessage message : messages) {
                // Only update messages NOT sent by this user
                if (!message.getSender().getId().equals(userId)) {

                    Optional<MessageStatus> statusOpt = messageStatusRepository
                            .findByMessageIdAndUserId(message.getId(), userId);

                    if (statusOpt.isPresent()) {
                        MessageStatus status = statusOpt.get();

                        if (status.getStatus() != MessageStatus.Status.READ) {
                            status.setStatus(MessageStatus.Status.READ);
                            status.setReadAt(LocalDateTime.now());
                            messageStatusRepository.save(status);
                            updatedCount++;

                            System.out.println("✅ Marked message " + message.getId() + " as READ");
                        }
                    }
                }
            }

            System.out.println("✅ Updated " + updatedCount + " messages to READ");

        } catch (Exception e) {
            System.err.println("❌ Error in handleRead: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
