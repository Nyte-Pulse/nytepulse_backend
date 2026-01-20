package NytePulse.backend.controller;

import NytePulse.backend.dto.StartStreamRequest;
import NytePulse.backend.dto.StreamResponse;
import NytePulse.backend.service.centralServices.LiveStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/streams")
public class LiveStreamController {

    @Autowired
    private LiveStreamService liveStreamService;

    @PostMapping("/start")
    public ResponseEntity<StreamResponse> goLive(@RequestBody StartStreamRequest request) {
        return ResponseEntity.ok(liveStreamService.startStream(request));
    }
}