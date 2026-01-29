package NytePulse.backend.controller;

import NytePulse.backend.entity.MusicTrack;
import NytePulse.backend.service.centralServices.MusicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadTrack(
            @RequestParam("title") String title,
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("image") MultipartFile imageFile) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (audioFile.isEmpty() || imageFile.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Audio and Image files are required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            return musicService.uploadTrack(title, audioFile, imageFile);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Upload failed: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTracks() {
        return musicService.getAllTracks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTrackById(@PathVariable Long id) {
        return musicService.getTrackById(id);
    }
}
