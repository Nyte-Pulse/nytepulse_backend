package NytePulse.backend.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "media")
@Data
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType; // image/jpeg, video/mp4, etc.
    private String bunnyUrl; // CDN URL from BunnyNet
    private String bunnyVideoId; // For Bunny Stream videos
    private Long fileSize;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now(ZoneId.of("Asia/Colombo"));
//        updatedAt = LocalDateTime.now();
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = true)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("postId")  // This names the field "postId" in JSON
    private Post post;

    @ManyToOne
    @JoinColumn(name = "story_id")
    @JsonBackReference
    private Story story;

    public enum MediaType {
        IMAGE, VIDEO
    }

    public Story getStory() {
        return story;
    }

    public void setStory(Story story) {
        this.story = story;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getBunnyUrl() {
        return bunnyUrl;
    }

    public void setBunnyUrl(String bunnyUrl) {
        this.bunnyUrl = bunnyUrl;
    }

    public String getBunnyVideoId() {
        return bunnyVideoId;
    }

    public void setBunnyVideoId(String bunnyVideoId) {
        this.bunnyVideoId = bunnyVideoId;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

}