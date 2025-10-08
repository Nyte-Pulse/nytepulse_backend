package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.*;
import org.springframework.data.domain.Page;
import java.util.List;

public interface ChatService {

    ConversationDTO createOrGetPrivateConversation(Long userId1, Long userId2);

    ConversationDTO createGroupConversation(Long creatorId, List<Long> participantIds, String groupName);

    List<ConversationDTO> getUserConversations(Long userId);

    ChatMessageDTO sendMessage(Long conversationId, Long senderId, String content, String messageType, String fileUrl);

    Page<ChatMessageDTO> getConversationMessages(Long conversationId, Long userId, int page, int size);

    void markConversationAsRead(Long conversationId, Long userId);

    Integer getUnreadCount(Long conversationId, Long userId);

    void deleteMessage(Long messageId, Long userId);
}
