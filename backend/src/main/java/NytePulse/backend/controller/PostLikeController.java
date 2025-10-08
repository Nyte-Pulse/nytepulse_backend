package NytePulse.backend.controller;

import NytePulse.backend.service.centralServices.PostLikeService;;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post/{postId}")
@RequiredArgsConstructor
public class PostLikeController {

    @Autowired
    private PostLikeService postLikeService;

    // Toggle like on a post
    @PostMapping("/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long postId, @RequestHeader("User-Id") Long userId) {

        return postLikeService.toggleLike(postId, userId);
    }

    // Get like count for a post
    @GetMapping("/likes/count")
    public ResponseEntity<?> getLikeCount(@PathVariable Long postId) {

        return postLikeService.getLikeCount(postId);
    }

    // Check if post is liked by user
    @GetMapping("/likes/status")
    public ResponseEntity<?> checkLikeStatus(@PathVariable Long postId, @RequestHeader("User-Id") Long userId) {
        return postLikeService.isPostLikedByUser(postId, userId);

    }

    // Get post statistics (likes + comments)
    @GetMapping("/stats")
    public ResponseEntity<?> getPostStats(@PathVariable Long postId, @RequestHeader("User-Id") Long userId) {

        return postLikeService.getPostStats(postId, userId);

    }
}
