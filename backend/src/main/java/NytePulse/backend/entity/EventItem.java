package NytePulse.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lookup_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category; // e.g., "EVENT_CATEGORY", "AMENITY", etc.

    @Column(nullable = false)
    private String value;
}