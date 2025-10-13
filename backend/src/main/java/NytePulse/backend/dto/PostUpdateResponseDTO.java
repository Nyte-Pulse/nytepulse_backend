
package NytePulse.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PostUpdateResponseDTO {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
    private List<String> removedMediaFiles;
    private List<String> addedMediaFiles;

    // Constructors
    public PostUpdateResponseDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getRemovedMediaFiles() { return removedMediaFiles; }
    public void setRemovedMediaFiles(List<String> removedMediaFiles) { this.removedMediaFiles = removedMediaFiles; }

    public List<String> getAddedMediaFiles() { return addedMediaFiles; }
    public void setAddedMediaFiles(List<String> addedMediaFiles) { this.addedMediaFiles = addedMediaFiles; }
}
