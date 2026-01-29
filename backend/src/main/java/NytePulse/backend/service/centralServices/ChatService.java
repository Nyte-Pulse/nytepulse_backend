package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ChatService {

    ResponseEntity<?> createOrGetPrivateConversation(Long userId1, Long userId2);

    ResponseEntity<?> createGroupConversation(Long creatorId, List<Long> participantIds, String groupName);

    ResponseEntity<?> getUserConversations(Long userId);

    ChatMessageDTO sendMessage(Long conversationId, Long senderId, String content, String messageType, String fileUrl);

    ResponseEntity<?> getConversationMessages(Long conversationId, Long userId, int page, int size);

    ResponseEntity<?> markConversationAsRead(Long conversationId, Long userId);

    ResponseEntity<?> getUnreadCount(Long conversationId, Long userId);

    ResponseEntity<?> deleteMessage(Long messageId, Long userId);

    ResponseEntity<?> getUnreadConversations(Long userId);

    ResponseEntity<?> acceptMessageRequest(Long conversationId, Long userId);

    ResponseEntity<?> getMessageRequests(Long userId);
}
