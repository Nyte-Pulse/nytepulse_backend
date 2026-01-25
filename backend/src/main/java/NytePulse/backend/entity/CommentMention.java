package NytePulse.backend.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "comment_mentions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentMention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the specific Comment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    @JsonIgnore
    private Comment comment;

    // Link to the Post (Stored here for faster querying of "mentions on a post")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;

    // The User who is being mentioned
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioned_user_id", nullable = false)
    private User mentionedUser;

    // The User who created the comment (The person tagging)
    @Column(name = "creator_user_id")
    private String creatorUserId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}