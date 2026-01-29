package NytePulse.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "music_tracks")
public class MusicTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "audio_url", nullable = false)
    private String audioUrl;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    // To store the path in Bunny for potential deletion later
    @Column(name = "storage_path")
    private String storagePath;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    // Constructors
    public MusicTrack() {}

    public MusicTrack(String title, String audioUrl, String coverImageUrl, String storagePath) {
        this.title = title;
        this.audioUrl = audioUrl;
        this.coverImageUrl = coverImageUrl;
        this.storagePath = storagePath;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
