package NytePulse.backend.repository;


import NytePulse.backend.entity.MusicTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MusicTrackRepository extends JpaRepository<MusicTrack, Long> {
    // You can add custom finders here if needed, e.g., findByTitleContaining
}
