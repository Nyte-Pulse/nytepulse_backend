package NytePulse.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "live_streams")
@Data
public class LiveStream {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // The ID provided by Bunny.net (the GUID)
    private String bunnyVideoId;

    // Status: PENDING, LIVE, ENDED
    private String status;

    private LocalDateTime createdAt;

    // Relationship: Many streams can belong to One user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User broadcaster;
}