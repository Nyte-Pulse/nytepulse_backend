package NytePulse.backend.repository;

import NytePulse.backend.entity.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {

    UserDetails findByUserId(String userId);
    UserDetails findByEmail(String email);
    UserDetails findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Page<UserDetails> findByNameContainingIgnoreCase(String name, Pageable pageable);
}