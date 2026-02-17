package NytePulse.backend.dto;

import lombok.Data;

@Data
public class LikedUserDTO {

    private Long userDetailsId;
    private String userId;
    private String username;
    private String name;
    private String profilePicture;
    private String bio;

    private String reactionType;
}