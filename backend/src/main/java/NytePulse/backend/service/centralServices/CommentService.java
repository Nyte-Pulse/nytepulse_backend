package NytePulse.backend.service.centralServices;


import NytePulse.backend.dto.CommentRequestDTO;
import NytePulse.backend.dto.CommentResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CommentService {

    ResponseEntity<?> addComment(Long postId, Long userId, CommentRequestDTO commentRequestDTO);

    ResponseEntity<?> getCommentsByPostId(Long postId);

    ResponseEntity<?> updateComment(Long commentId, Long userId, CommentRequestDTO commentRequestDTO);

    ResponseEntity<?> deleteComment(Long commentId, Long userId);

    ResponseEntity<?> getCommentCount(Long postId);

}
