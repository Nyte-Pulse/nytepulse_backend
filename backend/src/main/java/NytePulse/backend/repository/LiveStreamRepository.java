package NytePulse.backend.repository;

import NytePulse.backend.entity.LiveStream;
import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LiveStreamRepository extends JpaRepository<LiveStream, Long> {
    Optional<LiveStream> findByStreamKey(String streamKey);
    Optional<LiveStream> findByUser_Id(Long userId);

    // To check if a specific user currently has an active stream
    boolean existsByUser_Id(Long userId);
}