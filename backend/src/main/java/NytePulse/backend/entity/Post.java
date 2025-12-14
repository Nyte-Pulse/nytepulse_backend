package NytePulse.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    String tagFriendId;
    String mentionFriendId;
    String location;
    private LocalDateTime createdAt = LocalDateTime.now();


    private LocalDateTime updatedAt;

    @Column(name = "share_count", columnDefinition = "bigint default 0")
    private Long shareCount = 0L;  // ADD THIS FIELD

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> media;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getShareCount() {
        return shareCount;
    }
    public void setShareCount(Long shareCount) {
        this.shareCount = shareCount;
    }

    public String getTagFriendId() {
        return tagFriendId;
    }

    public void setTagFriendId(String tagFriendId) {
        this.tagFriendId = tagFriendId;
    }

    public String getMentionFriendId() {
        return mentionFriendId;
    }

    public void setMentionFriendId(String mentionFriendId) {
        this.mentionFriendId = mentionFriendId;
    }

    public String getLocation() {
        return location;
    }

    
    public void setLocation(String location) {
        this.location = location;
    }
}