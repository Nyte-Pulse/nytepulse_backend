package NytePulse.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LiveStreamResponseDTO {
    private String streamKey;
    private String ingestUrl;
    private String playbackUrl;
    private String visibility;
}