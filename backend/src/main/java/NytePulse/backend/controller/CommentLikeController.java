package NytePulse.backend.controller;

import NytePulse.backend.service.centralServices.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/comments/{commentId}")
@RequiredArgsConstructor
public class CommentLikeController {

    @Autowired
    private CommentLikeService commentLikeService;

    @PostMapping("/like")
    public ResponseEntity<?> toggleCommentLike(@PathVariable Long commentId, @RequestHeader("User-Id") Long userId) {

        return commentLikeService.toggleCommentLike(commentId, userId);
    }


    @GetMapping("/likes/count")
    public ResponseEntity<?> getCommentLikeCount(@PathVariable Long commentId) {

        return commentLikeService.getCommentLikeCount(commentId);
    }

}
