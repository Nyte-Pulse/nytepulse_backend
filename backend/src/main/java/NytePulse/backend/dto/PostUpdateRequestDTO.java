
package NytePulse.backend.dto;

import java.util.List;

public class PostUpdateRequestDTO {
    private String content;
    private List<Long> removeMediaIds;
    private String userId;

    // Constructors
    public PostUpdateRequestDTO() {}

    public PostUpdateRequestDTO(String content, List<Long> removeMediaIds, String userId) {
        this.content = content;
        this.removeMediaIds = removeMediaIds;
        this.userId = userId;
    }

    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<Long> getRemoveMediaIds() { return removeMediaIds; }
    public void setRemoveMediaIds(List<Long> removeMediaIds) { this.removeMediaIds = removeMediaIds; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
