package NytePulse.backend.dto;

import java.time.LocalDateTime;

public class UserProfileDTO {
    private String userId;
    private String username;
    private String email;
    private String name;
    private String bio;
    private String profilePicture;
    private String accountType;
    private String status;
    private String profileUrl;
    private Boolean isPrivate;
    private String gender;
    private LocalDateTime dateTimeCreated;

    // Club-specific fields
    private String contactPhone;
    private Long followersCount;
    private Long eventsPublishedCount;
    private Float ratingAvg;

    // Getters and Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDateTime getDateTimeCreated() {
        return dateTimeCreated;
    }

    public void setDateTimeCreated(LocalDateTime dateTimeCreated) {
        this.dateTimeCreated = dateTimeCreated;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }

    public Long getEventsPublishedCount() {
        return eventsPublishedCount;
    }

    public void setEventsPublishedCount(Long eventsPublishedCount) {
        this.eventsPublishedCount = eventsPublishedCount;
    }

    public Float getRatingAvg() {
        return ratingAvg;
    }

    public void setRatingAvg(Float ratingAvg) {
        this.ratingAvg = ratingAvg;
    }
}
