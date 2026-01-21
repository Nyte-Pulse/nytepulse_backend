package NytePulse.backend.service.centralServices;


import NytePulse.backend.dto.*;
import org.springframework.http.ResponseEntity;

public interface LiveStreamService {
    ResponseEntity<?> startStream(String userId, StartStreamRequestDTO request);
    void stopStream(String userId, String streamKey);
    ResponseEntity<?> checkStreamAccess(String viewerId);
}