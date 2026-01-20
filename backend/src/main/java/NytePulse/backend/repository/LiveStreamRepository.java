package NytePulse.backend.repository;

import NytePulse.backend.entity.LiveStream;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiveStreamRepository extends JpaRepository<LiveStream, Long> {
    // Find all streams that are currently LIVE
    List<LiveStream> findByStatus(String status);
}
