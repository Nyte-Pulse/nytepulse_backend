package NytePulse.backend.entity;

import jakarta.persistence.*;


import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "user_details")
public class UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_details_id")
    private Long userDetailsId;

    // Change this to store userId as String directly
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(length = 500)
    private String bio;

    @Column(name = "profile_picture_id")
    private Long profilePictureId;

    @Column(length = 10)
    private String gender;

    private LocalDateTime birthday;

    @Column(name = "is_private")
    private Boolean isPrivate = false;

    @Column(name = "date_time_created")
    private LocalDateTime dateTimeCreated;

    @Column(length = 10)
    private String status = "active"; // active, blocked

    @Column(name = "account_type", length = 20)
    private String accountType; // personal, business

    @Column(length = 100)
    private String name;

    // Constructors
    public UserDetails() {
    }

    public UserDetails(String userId, String email, String username, String name, String accountType) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.name = name;
        this.accountType = accountType;
        this.status = "active";
        this.isPrivate = false;
        this.dateTimeCreated = LocalDateTime.now(ZoneId.of("Asia/Colombo"));
    }

    // Getters and Setters
    public Long getUserDetailsId() {
        return userDetailsId;
    }

    public void setUserDetailsId(Long userDetailsId) {
        this.userDetailsId = userDetailsId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Long getProfilePictureId() {
        return profilePictureId;
    }

    public void setProfilePictureId(Long profilePictureId) {
        this.profilePictureId = profilePictureId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDateTime getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDateTime birthday) {
        this.birthday = birthday;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public LocalDateTime getDateTimeCreated() {
        return dateTimeCreated;
    }

    public void setDateTimeCreated(LocalDateTime dateTimeCreated) {
        this.dateTimeCreated = dateTimeCreated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
