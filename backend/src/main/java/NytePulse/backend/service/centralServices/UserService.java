package NytePulse.backend.service.centralServices;

import NytePulse.backend.auth.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface UserService {

    ResponseEntity<?> register(RegisterRequest request);

    ResponseEntity<?> followUser(String followerUserId, String followingUserId);

    ResponseEntity<?> getFollowers(String userId);

    ResponseEntity<?> getFollowing(String userId);

    ResponseEntity<?> unfollowUser(String userId, String followingUserId);

    Boolean isFollowing(String userId, String targetUserId);

    ResponseEntity<?> getFollowersCount(String userId);

    ResponseEntity<?> getFollowingCount(String userId);
}
