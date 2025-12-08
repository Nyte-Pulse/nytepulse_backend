package NytePulse.backend.repository;

import NytePulse.backend.entity.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, Long> {

    Optional<MessageStatus> findByMessageId(Long messageId);

    Optional<MessageStatus> findByMessageIdAndUserId(Long messageId, Long userId);

}
