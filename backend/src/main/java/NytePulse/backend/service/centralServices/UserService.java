package NytePulse.backend.service.centralServices;

import NytePulse.backend.auth.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    ResponseEntity<?> register(RegisterRequest request);

    ResponseEntity<?> followUser(String followerUserId, String followingUserId);

    ResponseEntity<?> getFollowers(String userId);

    ResponseEntity<?> getFollowing(String userId);

    ResponseEntity<?> unfollowUser(String userId, String followingUserId);

    Boolean isFollowing(String userId, String targetUserId);

    ResponseEntity<?> getFollowersCount(String userId);

    ResponseEntity<?> getFollowingCount(String userId);

    ResponseEntity<?> getUserByUsername(String username);

    ResponseEntity<?> checkUsernameAvailability(String username);

    ResponseEntity<?> uploadProfilePicture(MultipartFile file, String userId);

    ResponseEntity<?> updateProfilePicture(MultipartFile file, String userId, String oldFileName);

    ResponseEntity<?> deleteProfilePicture(String fileName,String userId);
}