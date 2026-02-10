package NytePulse.backend.controller;

import NytePulse.backend.dto.BunnyNetUploadResult;
import NytePulse.backend.dto.FeedbackRequest;
import NytePulse.backend.dto.UserDetailsDto;
import NytePulse.backend.service.BunnyNetService;
import NytePulse.backend.service.centralServices.UserDetailsService;
import NytePulse.backend.service.centralServices.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/user-details")
@CrossOrigin(originPatterns = "*")
public class UserController {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;


    @Autowired
    private BunnyNetService bunnyNetService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserDetails(
            @PathVariable String userId,
            @RequestBody UserDetailsDto userDetailsDto) {

        return userDetailsService.updateUserDetails(userId, userDetailsDto);
    }

    @PatchMapping("/{userId}/bio")
    public ResponseEntity<?> updateBio(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {

        UserDetailsDto updateRequest = new UserDetailsDto();
        updateRequest.setBio(request.get("bio"));

        return userDetailsService.updateUserDetails(userId, updateRequest);
    }

    @PatchMapping("/{userId}/gender")
    public ResponseEntity<?> updateGender(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {

        UserDetailsDto updateRequest = new UserDetailsDto();
        updateRequest.setGender(request.get("gender"));

        return userDetailsService.updateUserDetails(userId, updateRequest);
    }

    @PatchMapping("/{userId}/profile-picture")
    public ResponseEntity<?> updateProfilePicture(
            @PathVariable String userId,
            @RequestBody Map<String, Long> request) {

        UserDetailsDto updateRequest = new UserDetailsDto();
        updateRequest.setProfilePictureId(request.get("profile_picture_id"));

        return userDetailsService.updateUserDetails(userId, updateRequest);
    }

    @PostMapping("/{userId}/follow/{followingUserId}")
    public ResponseEntity<?> followUser(@PathVariable String userId, @PathVariable String followingUserId) {
        return userService.followUser(userId, followingUserId);
    }

    @PostMapping("/followRequest/{userId}/follow/{followingUserId}")
    public ResponseEntity<?> sendFollowRequest(@PathVariable String userId, @PathVariable String followingUserId) {
        return userService.sendFollowRequest(userId, followingUserId);
    }

    @PutMapping("/acceptOrRejectedFollowRequest/{userId}/follow/{followingUserId}/{status}")
    public ResponseEntity<?> acceptOrRejectedFollowRequest(@PathVariable String userId, @PathVariable String followingUserId, @PathVariable String status) {
        return userService.acceptOrRejectedFollowRequest(userId, followingUserId,status);
    }

    @PostMapping("/{userId}/block/{followingUserId}")
    public ResponseEntity<?> blockUser(@PathVariable String userId, @PathVariable String followingUserId) {
        return userService.blockUser(userId, followingUserId);
    }

    @GetMapping("/getBlockListByUserId/{userId}")
    public ResponseEntity<?> getBlockListByUserId(@PathVariable Long userId) {
        return userService.getBlockListByUserId(userId);
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<?> getFollowers(@PathVariable String userId,Authentication authentication,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        String currentLoginUserId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            // Assuming your principal is the username or you have a custom UserDetails
            currentLoginUserId = authentication.getName();
        }
        return userService.getFollowers(userId,currentLoginUserId,page, size);
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<?> getFollowing(@PathVariable String userId, Authentication authentication,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        String currentLoginUserId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            // Assuming your principal is the username or you have a custom UserDetails
            currentLoginUserId = authentication.getName();
        }
        return userService.getFollowing(userId,currentLoginUserId,page,size);
    }

    @DeleteMapping("/{userId}/unfollow/{followingUserId}")
    public ResponseEntity<?> unfollowUser(@PathVariable String userId, @PathVariable String followingUserId) {
        return userService.unfollowUser(userId, followingUserId);
    }

    @DeleteMapping("/{userId}/unblock/{followingUserId}")
    public ResponseEntity<?> unblock(@PathVariable String userId, @PathVariable String followingUserId) {
        return userService.unblock(userId, followingUserId);
    }

    @GetMapping("/{userId}/follow-status/{targetUserId}")
    public ResponseEntity<?> getFollowStatus(@PathVariable String userId, @PathVariable String targetUserId) {

        boolean isFollowing = userService.isFollowing(userId, targetUserId);
        boolean isFollowedBy = userService.isFollowing(targetUserId, userId);
        boolean isBlockedBy = userService.isBlocked(userId, targetUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("isFollowing", isFollowing);
        response.put("isFollowedBy", isFollowedBy);
        response.put("isBlockedBy", isBlockedBy);
        response.put("userId", userId);
        response.put("targetUserId", targetUserId);
        response.put("status", HttpStatus.OK.value());

        return ResponseEntity.ok(response);

    }

    @GetMapping("FollowersCount/{userId}")
    public ResponseEntity<?> getFollowersCount(@PathVariable String userId) {
        return userService.getFollowersCount(userId);
    }

    @GetMapping("FollowingCount/{userId}")
    public ResponseEntity<?> getFollowingCount(@PathVariable String userId) {
        return userService.getFollowingCount(userId);
    }

    @GetMapping("/getUserByUsername/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @PostMapping("/setAccountPrivateorPublic")
    public ResponseEntity<?> setAccountPrivateOrPublic(@RequestParam String userId, @RequestParam Boolean isPrivate) {
        return userDetailsService.setAccountPrivateOrPublic(userId, isPrivate);
    }

    @GetMapping("checkUsernameAvailability/{username}")
    public ResponseEntity<?> checkUsernameAvailability(@PathVariable String username) {
        return userService.checkUsernameAvailability(username);
    }

    @GetMapping("/checkEmailAvailability/{email}")
    public ResponseEntity<?> checkEmailAvailability(@PathVariable String email) {
        return userDetailsService.checkEmailAvailability(email);
    }

    @PostMapping("/profilePicture/upload")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file, @RequestParam("userId") String userId) {
        return userService.uploadProfilePicture(file, userId);
    }

    @PutMapping("/profilePicture/update")
    public ResponseEntity<?> updateProfilePicture(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "oldFileName", required = false) String oldFileName) {

        return userService.updateProfilePicture(file, userId, oldFileName);
    }

    @GetMapping("/getProfilePicture/{fileName}")
    public ResponseEntity<?> getProfilePictureUrl(@PathVariable String fileName) {
        try {
            String cdnUrl = bunnyNetService.getProfilePictureUrl(fileName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", HttpStatus.OK.value());
            response.put("fileName", fileName);
            response.put("cdnUrl", cdnUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting profile picture URL: {}", e.getMessage());
            Map<String, Object> erroresponse = new HashMap<>();
            erroresponse.put("message", "Failed to get profile picture URL "+ e.getMessage());
            erroresponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.ok(erroresponse);
        }
    }

    @DeleteMapping("/profilePicture/delete")
    public ResponseEntity<?> deleteProfilePicture(@RequestParam String fileName,@RequestParam  String userId) {
        return userService.deleteProfilePicture(fileName,userId);
    }

    @GetMapping("/getAccountNameByEmail")
    public ResponseEntity<?> getAccountNameByEmail(@RequestParam String email) {
        return userDetailsService.getAccountNameByEmail(email);
    }

    @GetMapping("/searchAccountByName/{name}")
    public ResponseEntity<?> searchAccountByName(
            @PathVariable String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (name == null || name.trim().length() < 1) {
            return ResponseEntity
                    .badRequest()
                    .body("Please enter at least 1 characters to search");
        }

        Pageable pageable = PageRequest.of(page, size);
        return userDetailsService.searchAccountByName(name.trim(), pageable);
    }

    @GetMapping("/searchPostByPublishedUser/{name}")
    public ResponseEntity<?> searchPostByPublishedUser(
            @PathVariable String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        if (name == null || name.trim().length() < 1) {
            return ResponseEntity
                    .badRequest()
                    .body("Please enter at least 1 characters to search");
        }

        Pageable pageable = PageRequest.of(page, size);
        return userDetailsService.searchPostByPublishedUsesName(name.trim(), pageable);
    }

    @GetMapping("/searchFollowerAccountByName/{name}")
    public ResponseEntity<?> searchFollowerAccountByName(
            @RequestParam Long userId,
            @PathVariable String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (name == null || name.trim().length() < 1) {
            return ResponseEntity
                    .badRequest()
                    .body("Please enter at least 1 characters to search");
        }

        Pageable pageable = PageRequest.of(page, size);
        return userDetailsService.searchFollowerAccountByName(userId, name.trim(), pageable);
    }

    @GetMapping("/getAllBusinessAccount")
    public  ResponseEntity<?> getAllBusinessAccount(){
        return userDetailsService.getAllBusinessAccount();
    }

    @GetMapping("/getMentionedAllowUserList/{currentUserId}")
    public ResponseEntity<?> getMentionedAllowUserList(@PathVariable Long currentUserId) {
        return userDetailsService.getMentionedAllowUserList(currentUserId);
    }

    @GetMapping("/getTaggedAllowUserList/{currentUserId}")
    public ResponseEntity<?> getTaggedAllowUserList(@PathVariable Long currentUserId) {
        return userDetailsService.getTaggedAllowUserList(currentUserId);
    }

    @PostMapping("/generateProfileShareLink/{userId}")
    public ResponseEntity<?> generateProfileShareLink(@PathVariable Long userId) {
        return userService.generateProfileShareLink(userId);
    }

    @GetMapping("/getMostFollowersCountUsers")
    public ResponseEntity<?> getMostFollowersCountUsers() {
        return userService.getMostFollowersCountUsers();
    }

    @PostMapping("/feedBack/add")
    public ResponseEntity<?> addFeedback(@RequestBody FeedbackRequest request) {
       return  userService.saveFeedback(request);
    }

    @GetMapping("/feedBack/all")
    public ResponseEntity<?> getAllFeedback(@RequestParam int page,@RequestParam int size) {
        return ResponseEntity.ok(userService.getAllFeedback(page, size));
    }

}