package NytePulse.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "close_friends",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "close_friend_user_id"}))
@Data
public class CloseFriend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;  // The user who creates the close friends list

    @Column(name = "close_friend_user_id", nullable = false)
    private String closeFriendUserId;  // The user added to close friends

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now(ZoneId.of("Asia/Colombo"));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCloseFriendUserId() {
        return closeFriendUserId;
    }

    public void setCloseFriendUserId(String closeFriendUserId) {
        this.closeFriendUserId = closeFriendUserId;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
