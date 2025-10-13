
package NytePulse.backend.dto;

public class ShareResponseDTO {
    private String shareUrl;
    private String shareText;
    private Long shareCount;
    private String message;

    // Constructors
    public ShareResponseDTO() {}

    public ShareResponseDTO(String shareUrl, String shareText, Long shareCount, String message) {
        this.shareUrl = shareUrl;
        this.shareText = shareText;
        this.shareCount = shareCount;
        this.message = message;
    }

    // Getters and Setters
    public String getShareUrl() { return shareUrl; }
    public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }

    public String getShareText() { return shareText; }
    public void setShareText(String shareText) { this.shareText = shareText; }

    public Long getShareCount() { return shareCount; }
    public void setShareCount(Long shareCount) { this.shareCount = shareCount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
