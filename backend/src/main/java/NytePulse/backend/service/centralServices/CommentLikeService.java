package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.LikeResponseDTO;
import org.springframework.http.ResponseEntity;

public interface CommentLikeService {

    ResponseEntity<?> toggleCommentLike(Long commentId, Long userId);

    ResponseEntity<?> getCommentLikeCount(Long commentId,String token);

    boolean isCommentLikedByUser(Long commentId, Long userId);

    ResponseEntity<?> getLikedUsersByPostId(Long postId);

    ResponseEntity<?> getLikedUsersByCommentId(Long commentId,int page,int size);
}
