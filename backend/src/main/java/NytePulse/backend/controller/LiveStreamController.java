package NytePulse.backend.controller;

import NytePulse.backend.dto.LiveStreamResponseDTO;
import NytePulse.backend.dto.StartStreamRequestDTO;
import NytePulse.backend.dto.StreamAccessResponseDTO;
import NytePulse.backend.service.centralServices.LiveStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/streams")
@RequiredArgsConstructor
public class LiveStreamController {
    private final LiveStreamService liveStreamService;

    @PostMapping("/start/{userId}")
    public ResponseEntity<?> startBroadcast(
            @PathVariable String userId,
            @RequestBody StartStreamRequestDTO request) {
        try {
            return liveStreamService.startStream(userId, request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stop/{userId}")
    public ResponseEntity<?> stopBroadcast(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {

        String streamKey = body.get("streamKey");

        try {
            liveStreamService.stopStream(userId, streamKey);
            Map<String, Object> res = new HashMap<>();
            res.put("status", HttpStatus.OK.value());
            res.put("message", "Stream ended and deleted successfully");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/check-access/{viewerId}")
    public ResponseEntity<?> checkAccess(
            @PathVariable String viewerId
    ) {
        try {
            return liveStreamService.checkStreamAccess(viewerId);

        } catch (RuntimeException e) {
            if ("ACCESS_DENIED".equals(e.getMessage())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("access", "denied");
                errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                errorResponse.put("message", "You do not have permission to watch this stream");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}