package NytePulse.backend.controller;

import NytePulse.backend.service.centralServices.ProfileLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileLinkController {

    @Autowired
    private ProfileLinkService profileLinkService;

    // Generate profile link by userId
    @GetMapping("/link/{userId}")
    public ResponseEntity<?> getProfileLink(@PathVariable String userId) {
       return profileLinkService.generateProfileLink(userId);
    }

    // Generate profile link by username
    @GetMapping("/link/username/{username}")
    public ResponseEntity<?> getProfileLinkByUsername(@PathVariable String username) {
        return profileLinkService.generateProfileLinkByUsername(username);
    }

    // Get user profile by username (for accessing via the link)
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        return profileLinkService.getUserProfileByUsername(username);
    }

    // Get club profile by username
    @GetMapping("/club/{username}")
    public ResponseEntity<?> getClubProfile(@PathVariable String username) {
        return  profileLinkService.getClubProfileByUsername(username);
    }
}
