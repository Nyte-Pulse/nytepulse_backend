package NytePulse.backend.service.impl;

import NytePulse.backend.dto.BunnyNetUploadResult;
import NytePulse.backend.entity.MusicTrack;
import NytePulse.backend.repository.MusicTrackRepository;
import NytePulse.backend.service.BunnyNetService;
import NytePulse.backend.service.centralServices.MusicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class MusicServiceImpl implements MusicService {

    private final MusicTrackRepository repository;
    private final RestTemplate restTemplate;

    @Autowired
    private BunnyNetService bunnyNetService;

    public MusicServiceImpl(MusicTrackRepository repository) {
        this.repository = repository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ResponseEntity<?> uploadTrack(String title, MultipartFile audioFile, MultipartFile imageFile) {
        try {
            if (title == null || title.trim().isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Title is required");
            }
            if (audioFile == null || audioFile.isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Audio file is required");
            }

            BunnyNetUploadResult audioResult = bunnyNetService.uploadMusicTrack(audioFile);

            BunnyNetUploadResult imageResult = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                imageResult = bunnyNetService.uploadMusicCover(imageFile);
            }

            MusicTrack track = new MusicTrack();
            track.setTitle(title);
            track.setAudioUrl(audioResult.getCdnUrl());
            track.setStoragePath(audioResult.getFileName());
            track.setUploadedAt(LocalDateTime.now());

            if (imageResult != null) {
                track.setCoverImageUrl(imageResult.getCdnUrl());
            }

            MusicTrack savedTrack = repository.save(track);

            HashMap<String ,Object> res=new HashMap<>();
            res.put("status",HttpStatus.OK.value());
            res.put("data",savedTrack);

            return ResponseEntity.ok(res);

        } catch (IOException e) {
            log.error("File upload failed", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload files to storage");
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        return ResponseEntity.status(status).body(response);
    }

    @Override
    public  ResponseEntity<?> getAllTracks() {
        HashMap<String,Object> res = new HashMap<>();
        res.put("status", HttpStatus.OK.value());
        res.put("data", repository.findAll());
        return ResponseEntity.ok(res);
    }

    @Override
    public ResponseEntity<?> getTrackById(Long id) {
        HashMap<String,Object> res = new HashMap<>();
        res.put("status", HttpStatus.OK.value());
        res.put("data", repository.findById(id));

        return ResponseEntity.ok(res);
    }
}
