package NytePulse.backend.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "feed-backs")
@Data
public class FeedBack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private int rating;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userDetails_id", nullable = true)
    private UserDetails userDetails;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "clubDetails_id", nullable = true)
    private ClubDetails clubDetails;


}
