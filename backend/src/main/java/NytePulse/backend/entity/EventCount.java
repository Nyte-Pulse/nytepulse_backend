package NytePulse.backend.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "event_count")
public class EventCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "count", nullable = false)
    private Long EventCount= 0L;

    public EventCount() {
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

    public Long getEventCount() {
        return EventCount;
    }

    public void setEventCount(Long eventCount) {
        EventCount = eventCount;
    }
}
