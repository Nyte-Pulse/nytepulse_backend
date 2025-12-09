package NytePulse.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "club_details")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ClubDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "profile_picture_id")
    private Long profilePictureId;

    @Column(name = "date_time_created")
    private LocalDateTime dateTimeCreated;

    @Column(name = "status", length = 10)
    private String status = "active";

    @Column(name = "account_type", length = 20)
    private String accountType;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "followers_count")
    private Long followersCount;

    @Column(name = "events_published_count")
    private Long eventsPublishedCount;

    @Column(name = "rating_avg")
    private Float ratingAvg;



    @Column(name = "profile_picture")
    private String ProfilePicture;

    @Column(name = "profile_picture_file_name")
    private String ProfilePictureFileName;

    // Constructors
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

    public String getProfilePicture() {
        return ProfilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        ProfilePicture = profilePicture;
    }

    public String getProfilePictureFileName() {
        return ProfilePictureFileName;
    }

    public void setProfilePictureFileName(String profilePictureFileName) {
        ProfilePictureFileName = profilePictureFileName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClubDetailsId() {
        return id;
    }

    public void setClubDetailsId(Long id) {
        this.id = id;
    }

    public Long getClubId() {
        return id;
    }

    public void setClubId(Long id) {
        this.id = id;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Long getProfilePictureId() { return profilePictureId; }
    public void setProfilePictureId(Long profilePictureId) { this.profilePictureId = profilePictureId; }

    public LocalDateTime getDateTimeCreated() { return dateTimeCreated; }
    public void setDateTimeCreated(LocalDateTime dateTimeCreated) { this.dateTimeCreated = dateTimeCreated; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public Long getFollowersCount() { return followersCount; }
    public void setFollowersCount(Long followersCount) { this.followersCount = followersCount; }

    public Long getEventsPublishedCount() { return eventsPublishedCount; }
    public void setEventsPublishedCount(Long eventsPublishedCount) { this.eventsPublishedCount = eventsPublishedCount; }

    public Float getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(Float ratingAvg) { this.ratingAvg = ratingAvg; }

}
