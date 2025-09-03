package NytePulse.backend.repository;

import NytePulse.backend.entity.EventCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface EventCountRepository extends JpaRepository<EventCount,Long> {

    EventCount findByUserId(String userId);

}
