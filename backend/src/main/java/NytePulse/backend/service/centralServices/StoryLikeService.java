package NytePulse.backend.service.centralServices;


import org.springframework.http.ResponseEntity;

public interface StoryLikeService {
    ResponseEntity<?> toggleLike(Long storyId, Long userId);
}