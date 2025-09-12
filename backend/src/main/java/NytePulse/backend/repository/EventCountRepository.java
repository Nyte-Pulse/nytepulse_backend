package NytePulse.backend.repository;

import NytePulse.backend.entity.CountEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EventCountRepository extends JpaRepository<CountEvent,Long> {

    CountEvent findByUserId(String userId);

}
