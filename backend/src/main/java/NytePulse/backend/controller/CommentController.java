package NytePulse.backend.controller;

import NytePulse.backend.dto.ApiResponse;
import NytePulse.backend.dto.CommentRequestDTO;
import NytePulse.backend.dto.CommentResponseDTO;
import NytePulse.backend.service.centralServices.CommentService;
import NytePulse.backend.service.centralServices.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post/{postId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<?> addComment(
            @PathVariable Long postId,
            @RequestHeader("User-Id") Long userId,
            @RequestBody CommentRequestDTO commentRequestDTO) {

            return commentService.addComment(postId, userId, commentRequestDTO);
    }

    @GetMapping
    public ResponseEntity<?> getCommentsByPost(@PathVariable Long postId) {

         return commentService.getCommentsByPostId(postId);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestHeader("User-Id") Long userId,
            @RequestBody CommentRequestDTO commentRequestDTO) {

            return commentService.updateComment(commentId, userId, commentRequestDTO);

    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestHeader("User-Id") Long userId) {

            return commentService.deleteComment(commentId, userId);

    }

    @GetMapping("/count")
    public ResponseEntity<?> getCommentCount(@PathVariable Long postId) {

          return commentService.getCommentCount(postId);

    }
}
