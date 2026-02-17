package NytePulse.backend.entity;

import NytePulse.backend.enums.ReactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_reactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post; // Assuming you have a Post entity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Assuming you have a User entity

    @Enumerated(EnumType.STRING) // Stores "FIRE", "LIKE" in DB. Use EnumType.ORDINAL for numbers (0,1,2)
    @Column(name = "reaction_type", nullable = false)
    private ReactionType reactionType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}