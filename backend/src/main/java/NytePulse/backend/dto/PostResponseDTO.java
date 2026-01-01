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
public class PostResponseDTO {
    private Long id;
    private String content;
    private String location;
    private String userId;
    private String username;
    private LocalDateTime createdAt;
    private Long shareCount;
    private Integer mediaCount;
    private Integer likesCount;
    private Integer commentsCount;
    private List<TaggedUserDTO> taggedUsers;
    private List<MentionedUserDTO> mentionedUsers;
    private List<MediaDTO> media;
}
