package NytePulse.backend.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name= "club_details")
public class ClubDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long clubId;

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

    @Column(name = "date_time_created")
    private LocalDateTime dateTimeCreated;

    @Column(length = 10)
    private String status = "active"; // active, blocked

    @Column(name = "account_type", length = 20)
    private String accountType; // personal, business

    @Column(length = 100)
    private String name;

    @Column(name = "contact_phone")
    private  String contactPhone;

    @Column(name = "followers_count")
    private Long followersCount;

    @Column(name = "events_published_count")
    private Long eventsPublishedCount;

    @Column(name = "rating_avg")
    private float ratingAvg;

    public ClubDetails() {
    }


    public ClubDetails(String userId, String email, String username, String name, String accountType) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.name = name;
        this.accountType = accountType;
        this.status = "active";
        this.dateTimeCreated = LocalDateTime.now(ZoneId.of("Asia/Colombo"));
    }

    public Long getClubDetailsId() {
        return clubId;
    }

    public void setClubDetailsId(Long clubDetailsId) {
        this.clubId = clubDetailsId;
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

    public float getRatingAvg() {
        return ratingAvg;
    }

    public void setRatingAvg(float ratingAvg) {
        this.ratingAvg = ratingAvg;
    }
}
