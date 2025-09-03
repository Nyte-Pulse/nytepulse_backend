package NytePulse.backend.repository;

import NytePulse.backend.entity.EventDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventDetailsRepository extends JpaRepository<EventDetails,Long>{

    @Query(value = "SELECT event_id FROM event_details WHERE event_id LIKE 'EV%' ORDER BY event_id DESC LIMIT 1", nativeQuery = true)
    String findLastEventId();
}
