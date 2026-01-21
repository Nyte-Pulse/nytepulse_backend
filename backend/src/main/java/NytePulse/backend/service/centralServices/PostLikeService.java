package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.PostStatsDTO;
import org.springframework.http.ResponseEntity;

public interface PostLikeService {

    ResponseEntity<?> toggleLike(Long postId, Long userId);

    ResponseEntity<?> getLikeCount(Long postId,String token);

    ResponseEntity<?> isPostLikedByUser(Long postId, Long userId);

    ResponseEntity<?> getPostStats(Long postId, Long userId);

    ResponseEntity<?> getLikedUsersByPostId(Long postId,int page,int size);
}