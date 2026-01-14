package NytePulse.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "event_organizers")
@Data
public class EventOrganizer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventDetails event;

    private Long userId;

    private String organizerName;
    private String organizerContact;
    private String organizerEmail;
    private String websiteUrl;

    private String role; // Optional: e.g., "Lead", "Sponsor", "Co-host"
}
