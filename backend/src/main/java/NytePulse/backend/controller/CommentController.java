package NytePulse.backend.controller;

import NytePulse.backend.dto.CommentRequestDTO;
import NytePulse.backend.service.centralServices.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/post/{postId}")
    public ResponseEntity<?> addComment(@PathVariable Long postId, @RequestHeader("User-Id") Long userId, @RequestBody CommentRequestDTO commentRequestDTO) {

        return commentService.addComment(postId, userId, commentRequestDTO);
    }

    @PostMapping("/story/{storyId}")
    public ResponseEntity<?> addCommentToStory(@PathVariable Long storyId, @RequestHeader("User-Id") Long userId, @RequestBody CommentRequestDTO commentRequestDTO){
        return commentService.addCommentToStory(storyId,userId,commentRequestDTO);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getCommentsByPost(@PathVariable Long postId) {

        return commentService.getCommentsByPostId(postId);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId, @RequestHeader("User-Id") Long userId, @RequestBody CommentRequestDTO commentRequestDTO) {

        return commentService.updateComment(commentId, userId, commentRequestDTO);

    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, @RequestHeader("User-Id") Long userId) {

        return commentService.deleteComment(commentId, userId);

    }

    @GetMapping("/post/{postId}/count")
    public ResponseEntity<?> getCommentCount(@PathVariable Long postId) {
        return commentService.getCommentCount(postId);
    }


    @PostMapping("/{commentId}/reply")
    public ResponseEntity<?> addReply(@PathVariable Long commentId, @RequestHeader("User-Id") Long userId, @RequestBody CommentRequestDTO commentRequestDTO) {

        return commentService.addReply(commentId, userId, commentRequestDTO);

    }

    @GetMapping("/post/{postId}/nested")
    public ResponseEntity<?> getCommentsWithReplies(@PathVariable Long postId, @RequestHeader("User-Id") Long userId,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {

        return commentService.getCommentsWithRepliesByPostId(postId, userId,page,size);

    }

}
