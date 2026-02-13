package NytePulse.backend.repository;

import NytePulse.backend.entity.ReservedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservedUserRepository extends JpaRepository<ReservedUser, Long> {
    boolean existsByUsername(String username);
}