package NytePulse.backend.repository;



import NytePulse.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
   User findByUserId(String userId);
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    User findTopByAccountTypeOrderByUserIdDesc(String accountType);
}