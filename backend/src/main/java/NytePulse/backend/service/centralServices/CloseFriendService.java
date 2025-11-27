package NytePulse.backend.service.centralServices;

import org.springframework.http.ResponseEntity;

public interface CloseFriendService {
    ResponseEntity<?> addCloseFriend(String userId, String closeFriendUserId);

    ResponseEntity<?> removeCloseFriend(String userId, String closeFriendUserId);

    ResponseEntity<?> getCloseFriends(String userId);

    boolean isCloseFriend(String userId, String checkUserId);
}
