package NytePulse.backend.controller;

import NytePulse.backend.dto.UserDetailsDto;
import NytePulse.backend.service.centralServices.UserDetailsService;
import NytePulse.backend.service.centralServices.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}