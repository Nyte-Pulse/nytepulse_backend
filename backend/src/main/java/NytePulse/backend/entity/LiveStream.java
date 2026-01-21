package NytePulse.backend.entity;

import NytePulse.backend.enums.StreamVisibility;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "live_streams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveStream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The Broadcaster

    @Column(nullable = false, unique = true)
    private String streamKey;

    @Column(nullable = false)
    private String ingestUrl; // RTMP URL for broadcaster

    @Column(nullable = false)
    private String playbackUrl; // HLS URL for viewers

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StreamVisibility visibility;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
    }
}