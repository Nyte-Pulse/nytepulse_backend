package NytePulse.backend.repository;

import NytePulse.backend.auth.RefreshToken;
import NytePulse.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);
    @Modifying
    int deleteByUser(User user);

    @Modifying
    int deleteByExpiryDateBefore(Instant now);
}