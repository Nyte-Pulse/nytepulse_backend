package NytePulse.backend.controller;

import NytePulse.backend.service.centralServices.PostLikeService;
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

    @PostMapping("/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long postId, @RequestHeader("User-Id") Long userId,@RequestParam(name = "type") String reactType) {

        return postLikeService.toggleLike(postId, userId,reactType);
    }

    @GetMapping("/likes/count")
    public ResponseEntity<?> getLikeCount(@PathVariable Long postId,@RequestHeader("Authorization") String token) {

        return postLikeService.getLikeCount(postId,token);
    }

    @GetMapping("/likes/status")
    public ResponseEntity<?> checkLikeStatus(@PathVariable Long postId, @RequestHeader("User-Id") Long userId) {
        return postLikeService.isPostLikedByUser(postId, userId);

    }

    @GetMapping("/stats")
    public ResponseEntity<?> getPostStats(@PathVariable Long postId, @RequestHeader("User-Id") Long userId) {

        return postLikeService.getPostStats(postId, userId);

    }

    @GetMapping("/getLikedUsersByPostId")
    public ResponseEntity<?> getLikedUsersByPostId(@PathVariable Long postId,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {

        return postLikeService.getLikedUsersByPostId(postId,page,size);
    }


}
