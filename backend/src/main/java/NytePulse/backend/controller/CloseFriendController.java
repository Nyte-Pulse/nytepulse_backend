package NytePulse.backend.controller;

import NytePulse.backend.service.centralServices.CloseFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/close-friends")
public class CloseFriendController {

    @Autowired
    private CloseFriendService closeFriendService;

    @PostMapping("/add")
    public ResponseEntity<?> addCloseFriend(
            @RequestParam("userId") String userId,
            @RequestParam("closeFriendUserId") String closeFriendUserId) {
        return closeFriendService.addCloseFriend(userId, closeFriendUserId);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeCloseFriend(
            @RequestParam("userId") String userId,
            @RequestParam("closeFriendUserId") String closeFriendUserId) {
        return closeFriendService.removeCloseFriend(userId, closeFriendUserId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getCloseFriends(@PathVariable String userId) {
        return closeFriendService.getCloseFriends(userId);
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkIsCloseFriend(
            @RequestParam("userId") String userId,
            @RequestParam("checkUserId") String checkUserId) {

        boolean isClose = closeFriendService.isCloseFriend(userId, checkUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("isCloseFriend", isClose);
        return ResponseEntity.ok(response);
    }
}
