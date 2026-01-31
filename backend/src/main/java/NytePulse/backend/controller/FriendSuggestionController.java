package NytePulse.backend.controller;


import NytePulse.backend.service.centralServices.FriendSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/suggestions")
@CrossOrigin(originPatterns = "*")
public class FriendSuggestionController {

    @Autowired
    private FriendSuggestionService friendSuggestionService;

    @GetMapping("/friends")
    public ResponseEntity<?> getFriendSuggestions(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit) {
        String currentUserId = authentication.getName();
             return friendSuggestionService.getFriendSuggestions(currentUserId, limit);
    }

    @GetMapping("/mutual-friends/{targetUserId}")
    public ResponseEntity<?> getMutualFriends(
            Authentication authentication,
            @PathVariable String targetUserId) {

        String currentUserId = authentication.getName();
            return friendSuggestionService.getMutualFriends(currentUserId, targetUserId);
    }
}
