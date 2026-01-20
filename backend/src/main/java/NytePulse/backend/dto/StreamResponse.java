package NytePulse.backend.dto;

import lombok.Data;

@Data
public class StreamResponse {
    private String streamId;      // Bunny Video ID
    private String rtmpUrl;       // Where Flutter pushes video
    private String streamKey;     // The key for the push
    private String playbackUrl;   // .m3u8 url for viewers
    private String broadcasterName; // To show on screen
}