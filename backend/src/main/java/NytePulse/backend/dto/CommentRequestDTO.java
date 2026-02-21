package NytePulse.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDTO {

    @NotBlank(message = "Comment content cannot be empty")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String content;

    private List<String> mentionedUserIds;

    private String mediaUrl;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}