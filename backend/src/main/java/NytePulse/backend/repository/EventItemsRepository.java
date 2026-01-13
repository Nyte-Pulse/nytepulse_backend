package NytePulse.backend.repository;

import NytePulse.backend.entity.EventItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventItemsRepository extends JpaRepository<EventItem, Long> {
    List<EventItem> findByCategory(String category);
}
