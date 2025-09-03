package NytePulse.backend.controller;

import NytePulse.backend.dto.UserDetailsDto;
import NytePulse.backend.service.centralServices.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/user-details")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Update user details (bio, birthday, profile_picture_id, gender)
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserDetails(
            @PathVariable String userId,
             @RequestBody UserDetailsDto userDetailsDto) {

        return userDetailsService.updateUserDetails(userId, userDetailsDto);
    }


    /**
     * Update bio only
     */
    @PatchMapping("/{userId}/bio")
    public ResponseEntity<?> updateBio(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {

        UserDetailsDto updateRequest = new UserDetailsDto();
        updateRequest.setBio(request.get("bio"));

        return userDetailsService.updateUserDetails(userId, updateRequest);
    }

    /**
     * Update gender only
     */
    @PatchMapping("/{userId}/gender")
    public ResponseEntity<?> updateGender(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {

        UserDetailsDto updateRequest = new UserDetailsDto();
        updateRequest.setGender(request.get("gender"));

        return userDetailsService.updateUserDetails(userId, updateRequest);
    }

    /**
     * Update profile picture ID only
     */
    @PatchMapping("/{userId}/profile-picture")
    public ResponseEntity<?> updateProfilePicture(
            @PathVariable String userId,
            @RequestBody Map<String, Long> request) {

        UserDetailsDto updateRequest = new UserDetailsDto();
        updateRequest.setProfilePictureId(request.get("profile_picture_id"));

        return userDetailsService.updateUserDetails(userId, updateRequest);
    }
}
