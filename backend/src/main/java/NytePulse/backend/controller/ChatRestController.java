package NytePulse.backend.controller;

import NytePulse.backend.dto.*;
import NytePulse.backend.service.centralServices.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    @Autowired
    private  ChatService chatService;


    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @GetMapping("/conversations")
    public ResponseEntity<?> getUserConversations(
            @RequestHeader("User-Id") Long userId) {

        try {
            List<ConversationDTO> conversations = chatService.getUserConversations(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", conversations
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Create or get private conversation
     */
    @PostMapping("/conversations/private/{otherUserId}")
    public ResponseEntity<?> createPrivateConversation(
            @PathVariable Long otherUserId,
            @RequestHeader("User-Id") Long userId) {

        try {;
            ConversationDTO conversation = chatService.createOrGetPrivateConversation(userId, otherUserId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", conversation
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Create group conversation
     */
    @PostMapping("/conversations/group")
    public ResponseEntity<?> createGroupConversation(
            @RequestBody Map<String, Object> payload,
            @RequestHeader("User-Id") Long userId) {

        try {
            String groupName = (String) payload.get("name");
            List<Long> participantIds = (List<Long>) payload.get("participantIds");

            ConversationDTO conversation = chatService.createGroupConversation(
                    userId,
                    participantIds,
                    groupName
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", conversation
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get messages for a conversation (paginated)
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestHeader("User-Id") Long userId) {

        try {
            Page<ChatMessageDTO> messages = chatService.getConversationMessages(
                    conversationId,
                    userId,
                    page,
                    size
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", messages.getContent(),
                    "page", page,
                    "totalPages", messages.getTotalPages(),
                    "totalElements", messages.getTotalElements()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get unread count for conversation
     */
    @GetMapping("/conversations/{conversationId}/unread")
    public ResponseEntity<?> getUnreadCount(
            @PathVariable Long conversationId,
            @RequestHeader("User-Id") Long userId) {

        try {
            Integer unreadCount = chatService.getUnreadCount(conversationId, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "unreadCount", unreadCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete message
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long messageId,
            @RequestHeader("User-Id") Long userId) {

        try {
            chatService.deleteMessage(messageId, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Message deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> sendMessageViaRest(
            @PathVariable Long conversationId,
            @RequestBody Map<String, String> payload,
            @RequestHeader("User-Id") Long userId) {

        String content = payload.get("content");

        ChatMessageDTO message = chatService.sendMessage(
                conversationId, userId, content, "TEXT", null
        );

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                message
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", message
        ));
    }

}
