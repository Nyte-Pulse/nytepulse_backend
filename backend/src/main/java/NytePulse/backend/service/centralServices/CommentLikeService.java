package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.LikeResponseDTO;
import org.springframework.http.ResponseEntity;

public interface CommentLikeService {

    ResponseEntity<?> toggleCommentLike(Long commentId, Long userId);

    ResponseEntity<?> getCommentLikeCount(Long commentId);

    boolean isCommentLikedByUser(Long commentId, Long userId);
}
