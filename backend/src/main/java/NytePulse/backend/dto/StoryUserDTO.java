package NytePulse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryUserDTO {
    private Long id;
    private String userId;
    private String username;
    private String accountType;
    private String profilePictureUrl;
}
