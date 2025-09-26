// PostShareInfoDTO.java
package NytePulse.backend.dto;

import java.time.LocalDateTime;

public class PostShareInfoDTO {
    private Long id;
    private String content;
    private String authorUsername;
    private String authorUserId;
    private LocalDateTime createdAt;
    private Long shareCount;
    private String shareUrl;
    private String firstMediaUrl;
    private String mediaType;

    // Constructors
    public PostShareInfoDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public String getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(String authorUserId) { this.authorUserId = authorUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getShareCount() { return shareCount; }
    public void setShareCount(Long shareCount) { this.shareCount = shareCount; }

    public String getShareUrl() { return shareUrl; }
    public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }

    public String getFirstMediaUrl() { return firstMediaUrl; }
    public void setFirstMediaUrl(String firstMediaUrl) { this.firstMediaUrl = firstMediaUrl; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
}
