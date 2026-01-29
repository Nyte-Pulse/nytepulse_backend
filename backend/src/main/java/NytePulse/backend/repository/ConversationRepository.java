package NytePulse.backend.repository;

import NytePulse.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {


    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.user.id = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 " +
            "WHERE p1.user.id = :userId1 AND p2.user.id = :userId2 AND c.type = 'PRIVATE'")
    Optional<Conversation> findPrivateConversationBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );

    @Query("SELECT DISTINCT c FROM Conversation c " +
            "JOIN ChatMessage m ON c.id = m.conversationId " +
            "JOIN MessageStatus ms ON m.id = ms.messageId " +
            "WHERE ms.userId = :userId AND ms.status != 'READ' " +
            "AND c.status = 'ACCEPTED' " +
            "ORDER BY c.updatedAt DESC") // <--- FIX: Sort by Conversation time, not Message time
    List<Conversation> findConversationsWithUnreadMessages(@Param("userId") Long userId);

    @Query("SELECT COUNT(ms) FROM MessageStatus ms " +
            "JOIN ChatMessage m ON ms.messageId = m.id " +
            "WHERE m.conversationId = :conversationId " +
            "AND ms.userId = :userId " +
            "AND ms.status != 'READ'")
    Long countUnreadForUser(@Param("conversationId") Long conversationId,
                            @Param("userId") Long userId);


    @Query("SELECT DISTINCT c FROM Conversation c " +
            "JOIN ConversationParticipant cp ON c.id = cp.conversation.id " +
            "WHERE cp.user.id = :userId " +
            "AND (c.status = 'ACCEPTED' OR c.type = 'GROUP')")
    List<Conversation> findInboxConversations(@Param("userId") Long userId);

    @Query("SELECT DISTINCT c FROM Conversation c " +
            "JOIN ConversationParticipant cp ON c.id = cp.conversation.id " +
            "WHERE cp.user.id = :userId " +
            "AND c.status = 'PENDING' " +
            "AND c.type = 'PRIVATE'")
    List<Conversation> findPendingRequests(@Param("userId") Long userId);
}
