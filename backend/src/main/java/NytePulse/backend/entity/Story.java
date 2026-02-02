package NytePulse.backend.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "stories")
@Data
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000,nullable = true)
    private String content;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Media> media;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Transient
    private MusicTrack musicTrackInfo;

    @Column(length = 1000,nullable = true)
    private Long musicTrackId;



    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_close_friends_only")
    private Boolean isCloseFriendsOnly = false;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusHours(24); // Expire after 24 hours
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Media> getMedia() {
        return media;
    }

    public void setMedia(List<Media> media) {
        this.media = media;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsCloseFriendsOnly() {
        return isCloseFriendsOnly;
    }

    public void setIsCloseFriendsOnly(Boolean closeFriendsOnly) {
        isCloseFriendsOnly = closeFriendsOnly;
    }

    public Boolean getCloseFriendsOnly() {
        return isCloseFriendsOnly;
    }

    public void setCloseFriendsOnly(Boolean closeFriendsOnly) {
        isCloseFriendsOnly = closeFriendsOnly;
    }

    public MusicTrack getMusicTrackInfo() {
        return musicTrackInfo;
    }

    public void setMusicTrackInfo(MusicTrack musicTrackInfo) {
        this.musicTrackInfo = musicTrackInfo;
    }

}
