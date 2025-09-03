package NytePulse.backend.repository;

import NytePulse.backend.entity.ClubDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubDetailsRepository extends JpaRepository<ClubDetails, Long> {

    ClubDetails findByUserId(String userId);
}
