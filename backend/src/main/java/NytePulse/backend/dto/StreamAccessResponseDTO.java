package NytePulse.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StreamAccessResponseDTO {
    private String access;
    private String message;
    private String viewUrl;
    private int status;
}