package NytePulse.backend.repository;

import NytePulse.backend.entity.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, Long> {

    Optional<MessageStatus> findByMessageId(Long messageId);

    Optional<MessageStatus> findByMessageIdAndUserId(Long messageId, Long userId);

    @Modifying
    @Query("UPDATE MessageStatus ms SET ms.status = 'READ', ms.readAt = :now " +
            "WHERE ms.userId = :userId AND ms.status != 'READ' AND ms.messageId IN " +
            "(SELECT m.id FROM ChatMessage m WHERE m.conversationId = :conversationId)")
    void markAllAsRead(@Param("userId") Long userId,
                       @Param("conversationId") Long conversationId,
                       @Param("now") LocalDateTime now);

    // Count unread specific to a user
    @Query("SELECT COUNT(ms) FROM MessageStatus ms WHERE ms.userId = :userId AND ms.messageId IN " +
            "(SELECT m.id FROM ChatMessage m WHERE m.conversationId = :conversationId) " +
            "AND ms.status != 'READ'")
    Long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

}
