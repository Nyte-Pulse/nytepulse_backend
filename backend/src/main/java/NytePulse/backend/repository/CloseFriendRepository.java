package NytePulse.backend.repository;

import NytePulse.backend.entity.CloseFriend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CloseFriendRepository extends JpaRepository<CloseFriend, Long> {

    // Get all close friends of a user
    List<CloseFriend> findByUserId(String userId);

    // Check if a user is in close friends list
    Optional<CloseFriend> findByUserIdAndCloseFriendUserId(String userId, String closeFriendUserId);

    // Check if user exists in close friends list (for story visibility)
    boolean existsByUserIdAndCloseFriendUserId(String userId, String closeFriendUserId);

    // Get all users who added this user to their close friends
    List<CloseFriend> findByCloseFriendUserId(String closeFriendUserId);

    // Count close friends
    long countByUserId(String userId);

    // Delete close friend
    void deleteByUserIdAndCloseFriendUserId(String userId, String closeFriendUserId);
}
