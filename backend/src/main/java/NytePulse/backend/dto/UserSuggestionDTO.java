package NytePulse.backend.dto;

public class UserSuggestionDTO {
    private String userId;
    private String username;
    private String name;
    private String profilePicture;
    private Long mutualFriendsCount;
    private String accountType;

    public UserSuggestionDTO(String userId, String username, String name,
                             String profilePicture, Long mutualFriendsCount, String accountType) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.profilePicture = profilePicture;
        this.mutualFriendsCount = mutualFriendsCount;
        this.accountType = accountType;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public Long getMutualFriendsCount() { return mutualFriendsCount; }
    public void setMutualFriendsCount(Long mutualFriendsCount) { this.mutualFriendsCount = mutualFriendsCount; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
}
