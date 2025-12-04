package NytePulse.backend.repository;

import NytePulse.backend.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(
            Long conversationId,
            Pageable pageable
    );

    @Query("SELECT m FROM ChatMessage m WHERE m.conversationId = :conversationId " +
            "AND m.isDeleted = false ORDER BY m.createdAt DESC LIMIT 1")
    Optional<ChatMessage> findLastMessageByConversationId(@Param("conversationId") Long conversationId);

    @Query("SELECT COUNT(m) FROM ChatMessage m JOIN ConversationParticipant cp " +
            "ON m.conversationId = cp.conversation.id " +
            "WHERE cp.user.id = :userId AND m.conversationId = :conversationId " +
            "AND m.createdAt > cp.lastReadAt AND m.sender.id != :userId")
    Long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    List<ChatMessage> findByConversationIdOrderByCreatedAtDesc(Long conversationId);

    Long countByConversationId(Long id);
}
