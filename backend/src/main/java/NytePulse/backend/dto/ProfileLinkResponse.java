package NytePulse.backend.dto;

public class ProfileLinkResponse {
    private String profileUrl;
    private String userId;
    private String username;
    private String accountType;
    private String qrCodeUrl; // Optional: for future QR code generation

    // Constructors
    public ProfileLinkResponse() {}

    public ProfileLinkResponse(String profileUrl, String userId, String username) {
        this.profileUrl = profileUrl;
        this.userId = userId;
        this.username = username;
    }

    // Getters and Setters
    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }
}
