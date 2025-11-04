package NytePulse.backend.repository;

import NytePulse.backend.entity.ClubDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClubDetailsRepository extends JpaRepository<ClubDetails, Long> {

    ClubDetails findByUserId(String userId);

    ClubDetails findByUsername(String username);

//    Optional<ClubDetails> findById(Long  id);
}
