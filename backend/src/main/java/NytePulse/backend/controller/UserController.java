package NytePulse.backend.controller;

import NytePulse.backend.dto.BunnyNetUploadResult;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/user-details")
@CrossOrigin(origins = "*", maxAge = 3600)
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

    @GetMapping("/{userId}/followers")
    public ResponseEntity<?> getFollowers(@PathVariable String userId) {
        ;
        return userService.getFollowers(userId);
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<?> getFollowing(@PathVariable String userId) {
        return userService.getFollowing(userId);
    }

    @DeleteMapping("/{userId}/unfollow/{followingUserId}")
    public ResponseEntity<?> unfollowUser(@PathVariable String userId, @PathVariable String followingUserId) {
        return userService.unfollowUser(userId, followingUserId);
    }

    @GetMapping("/{userId}/follow-status/{targetUserId}")
    public ResponseEntity<?> getFollowStatus(@PathVariable String userId, @PathVariable String targetUserId) {

        boolean isFollowing = userService.isFollowing(userId, targetUserId);
        boolean isFollowedBy = userService.isFollowing(targetUserId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("isFollowing", isFollowing);
        response.put("isFollowedBy", isFollowedBy);
        response.put("userId", userId);
        response.put("targetUserId", targetUserId);
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

    /**
     * Get profile picture URL
     */
//    @GetMapping("/url/{fileName}")
//    public ResponseEntity<?> getProfilePictureUrl(@PathVariable String fileName) {
//        try {
//            String cdnUrl = bunnyNetService.getProfilePictureUrl(fileName);
//
//            Map<String, String> response = new HashMap<>();
//            response.put("success", "true");
//            response.put("fileName", fileName);
//            response.put("cdnUrl", cdnUrl);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            log.error("Error getting profile picture URL: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(createErrorResponse("Failed to get profile picture URL"));
//        }
//    }

    /**
     * Download profile picture directly
     */
//    @GetMapping("/download/{fileName}")
//    public ResponseEntity<byte[]> downloadProfilePicture(@PathVariable String fileName) {
//        try {
//            byte[] imageBytes = bunnyNetService.downloadProfilePicture(fileName);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.IMAGE_JPEG);
//            headers.setContentLength(imageBytes.length);
//            headers.setCacheControl("public, max-age=31536000"); // Cache for 1 year
//
//            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
//
//        } catch (IOException e) {
//            log.error("Error downloading profile picture: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//    }

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

        if (name == null || name.trim().length() < 2) {
            return ResponseEntity
                    .badRequest()
                    .body("Please enter at least 2 characters to search");
        }

        Pageable pageable = PageRequest.of(page, size);
        return userDetailsService.searchAccountByName(name.trim(), pageable);
    }



}