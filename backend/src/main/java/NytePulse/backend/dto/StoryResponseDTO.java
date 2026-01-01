package NytePulse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryResponseDTO {
    private Long id;
    private String content;
    private List<StoryMediaDTO> media;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean isCloseFriendsOnly;
}