package NytePulse.backend.service.centralServices;

import NytePulse.backend.entity.MusicTrack;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface MusicService {
    ResponseEntity<?> uploadTrack(String title, MultipartFile audioFile, MultipartFile imageFile) throws IOException;
    ResponseEntity<?> getAllTracks();
    ResponseEntity<?> getTrackById(Long id);
}