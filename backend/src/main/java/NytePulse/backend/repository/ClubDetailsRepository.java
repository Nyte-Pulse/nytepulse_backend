package NytePulse.backend.repository;

import NytePulse.backend.entity.ClubDetails;
import NytePulse.backend.entity.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClubDetailsRepository extends JpaRepository<ClubDetails, Long> {

    ClubDetails findByUserId(String userId);

    ClubDetails findByUsername(String username);


    Page<ClubDetails> findByNameContainingIgnoreCase(String name, Pageable pageable);


    List<UserDetails> findByAccountType(String business);

    List<ClubDetails> findByUserIdIn(List<String> strings);

    Optional<ClubDetails> findByEmail(String email);
}
