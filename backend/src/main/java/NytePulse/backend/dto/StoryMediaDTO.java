package NytePulse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryMediaDTO {
    private Long id;
    private String bunnyUrl;
    private String mediaType;
    private String fileType;
}