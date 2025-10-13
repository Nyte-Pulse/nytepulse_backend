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
    public ResponseEntity<?> getUserConversations(@RequestHeader("User-Id") Long userId) {

           return chatService.getUserConversations(userId);

    }

    @PostMapping("/conversations/private/{otherUserId}")
    public ResponseEntity<?> createPrivateConversation(@PathVariable Long otherUserId, @RequestHeader("User-Id") Long userId) {

            return chatService.createOrGetPrivateConversation(userId, otherUserId);

    }

    @PostMapping("/conversations/group")
    public ResponseEntity<?> createGroupConversation(@RequestBody Map<String, Object> payload, @RequestHeader("User-Id") Long userId) {

            String groupName = (String) payload.get("name");
            List<Long> participantIds = (List<Long>) payload.get("participantIds");

            return chatService.createGroupConversation(
                    userId,
                    participantIds,
                    groupName
            );

    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> getConversationMessages(@PathVariable Long conversationId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size, @RequestHeader("User-Id") Long userId) {

           return chatService.getConversationMessages(
                    conversationId,
                    userId,
                    page,
                    size
            );
    }

    @GetMapping("/conversations/{conversationId}/unread")
    public ResponseEntity<?> getUnreadCount(@PathVariable Long conversationId, @RequestHeader("User-Id") Long userId) {

           return chatService.getUnreadCount(conversationId, userId);

    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long messageId,
            @RequestHeader("User-Id") Long userId) {

            return chatService.deleteMessage(messageId, userId);

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
