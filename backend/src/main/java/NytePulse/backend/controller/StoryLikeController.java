package NytePulse.backend.controller;
import NytePulse.backend.service.centralServices.StoryLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stories")
public class StoryLikeController {

    private final StoryLikeService storyLikeService;

    public StoryLikeController(StoryLikeService storyLikeService) {
        this.storyLikeService = storyLikeService;
    }

    @PostMapping("/{storyId}/toggle-like")
    public ResponseEntity<?> toggleStoryLike(
            @PathVariable Long storyId,
            @RequestParam Long userId) {

        return storyLikeService.toggleLike(storyId, userId);
    }
}
