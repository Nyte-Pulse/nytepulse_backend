package NytePulse.backend.repository;

import NytePulse.backend.entity.Notification;
import NytePulse.backend.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find all notifications for a user (paginated)
    Page<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    // Find unread notifications
    Page<Notification> findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    // Count unread notifications
    Long countByRecipient_IdAndIsReadFalse(Long recipientId);

    // Mark all as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isRead = false")
    void markAllAsReadByRecipientId(@Param("recipientId") Long recipientId);

    // Check if notification already exists (to prevent duplicates)
    Optional<Notification> findByRecipient_IdAndActor_IdAndTypeAndReferenceIdAndReferenceType(
            Long recipientId, Long actorId, NotificationType type, Long referenceId, String referenceType);

    // Delete old notifications (older than X days)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    void deleteOldNotifications(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
