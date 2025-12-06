package NytePulse.backend.repository;

import NytePulse.backend.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    Optional<UserSettings> findByUserId(Long userId);

    @Query("SELECT s FROM UserSettings s WHERE s.user.userId = :userId")
    Optional<UserSettings> findByUser_UserId(@Param("userId") Long  userId);
}
