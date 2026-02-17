package NytePulse.backend.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeResponseDTO {

    private boolean liked;

    private String currentReaction;
    private Long totalLikes;
    private String message;

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public Long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(Long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}