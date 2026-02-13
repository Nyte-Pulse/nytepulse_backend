package NytePulse.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reserved_users")
@Data
@NoArgsConstructor
public class ReservedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    public ReservedUser(String username) {
        this.username = username;
    }
}