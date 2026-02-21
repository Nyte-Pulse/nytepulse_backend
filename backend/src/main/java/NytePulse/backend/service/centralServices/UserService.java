package NytePulse.backend.service.centralServices;

import NytePulse.backend.auth.RegisterRequest;
import NytePulse.backend.auth.ResetPasswordByConfirmingOldRequest;
import NytePulse.backend.dto.FeedbackRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

public interface UserService {

    ResponseEntity<?> register(RegisterRequest request);

    ResponseEntity<?> followUser(String followerUserId, String followingUserId);

    ResponseEntity<?> getFollowers(String userId,String currentLoginUserId,int page,int size);

    ResponseEntity<?> getFollowing(String userId,String currentLoginUserId,int page,int size);

    ResponseEntity<?> unfollowUser(String userId, String followingUserId);

    Map<String,Object> isFollowing(String userId, String targetUserId);

    ResponseEntity<?> getFollowersCount(String userId);

    ResponseEntity<?> getFollowingCount(String userId);

    ResponseEntity<?> getUserByUsername(String username);

    ResponseEntity<?> checkUsernameAvailability(String username);

    ResponseEntity<?> uploadProfilePicture(MultipartFile file, String userId);

    ResponseEntity<?> updateProfilePicture(MultipartFile file, String userId, String oldFileName);

    ResponseEntity<?> deleteProfilePicture(String fileName,String userId);

    ResponseEntity<?> generateProfileShareLink(Long userId);

    ResponseEntity<?> blockUser(String userId, String followingUserId);

    ResponseEntity<?> unblock(String userId, String followingUserId);

    boolean isBlocked(String userId, String targetUserId);

    ResponseEntity<?> getMostFollowersCountUsers();

    ResponseEntity<?> getBlockListByUserId(Long userId);

    ResponseEntity<?> saveFeedback(FeedbackRequest request);

    ResponseEntity<?>  getAllFeedback(int page,int size);

    void setUserOnlineStatus(long parseLong, boolean b);

    ResponseEntity<?> sendFollowRequest(String userId, String followingUserId);

    ResponseEntity<?> acceptOrRejectedFollowRequest(String userId, String followingUserId,String status);

    ResponseEntity<?> resetPasswordByConfirmingOld(ResetPasswordByConfirmingOldRequest request,Long userId);

//  void updateUserStatus(Long userId, boolean isOnline);
}