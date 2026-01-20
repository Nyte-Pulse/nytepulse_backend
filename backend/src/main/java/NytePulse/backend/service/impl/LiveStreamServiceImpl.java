package NytePulse.backend.service.impl;

import NytePulse.backend.dto.StartStreamRequest;
import NytePulse.backend.dto.StreamResponse;
import NytePulse.backend.entity.LiveStream;
import NytePulse.backend.entity.User;
import NytePulse.backend.repository.LiveStreamRepository;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.service.centralServices.LiveStreamService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class LiveStreamServiceImpl implements LiveStreamService {

    @Value("${bunnynet.stream.api-key}")
    private String apiKey;

    @Value("${bunnynet.stream.library-id}")
    private String libraryId;

    // Usually: https://video.bunnycdn.com
    @Value("${bunnynet.stream.base-url}")
    private String bunnyBaseUrl;

    // Usually: https://{pull-zone}.b-cdn.net
    @Value("${bunnynet.stream.pull-zone-url}")
    private String pullZoneUrl;

    private final LiveStreamRepository streamRepo;
    private final UserRepository userRepo;
    private final RestTemplate restTemplate;

    public LiveStreamServiceImpl(LiveStreamRepository streamRepo, UserRepository userRepo) {
        this.streamRepo = streamRepo;
        this.userRepo = userRepo;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public StreamResponse startStream(StartStreamRequest request) {
        // 1. Fetch the User
        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Call Bunny.net to create the video object
        String createUrl = String.format("%s/library/%s/videos", bunnyBaseUrl, libraryId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("AccessKey", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("title", request.getTitle() + " by " + user.getUsername());

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(createUrl, entity, Map.class);

        // 3. Extract Bunny.net GUID
        Map<String, Object> data = response.getBody();
        String videoId = (String) data.get("guid");

        // 4. Save to Database
        LiveStream stream = new LiveStream();
        stream.setBroadcaster(user);
        stream.setTitle(request.getTitle());
        stream.setBunnyVideoId(videoId);
        stream.setStatus("LIVE");
        stream.setCreatedAt(LocalDateTime.now());
        streamRepo.save(stream);

        // 5. Construct Response
        StreamResponse resp = new StreamResponse();
        resp.setBroadcasterName(user.getUsername());
        resp.setStreamId(videoId);

        // Bunny.net Standard RTMP URL (Check your dashboard for exact URL)
        resp.setRtmpUrl("rtmp://rtmp-global.bunnycdn.com/live");
        resp.setStreamKey(videoId); // For Bunny, the Key is usually the Video GUID

        // The HLS URL for viewers
        resp.setPlaybackUrl(String.format("%s/%s/playlist.m3u8", pullZoneUrl, videoId));

        return resp;
    }
}
