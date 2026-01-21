package NytePulse.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StreamFeedItemDTO {
    private String streamKey;
    private String playbackUrl;
    private String broadcasterId;
    private String broadcasterName;
    private String broadcasterProfileUrl;
    private String broadcasterUsername;
    private String visibility;
}