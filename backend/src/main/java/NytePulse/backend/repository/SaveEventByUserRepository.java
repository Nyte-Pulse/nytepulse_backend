package NytePulse.backend.repository;

import NytePulse.backend.entity.SaveEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SaveEventByUserRepository extends JpaRepository<SaveEvent,Long> {
    List<SaveEvent> findByUserIdOrderByCreatedAtDesc(String userId);

    SaveEvent findByUserIdAndEventId(String userId, String eventId);
}
