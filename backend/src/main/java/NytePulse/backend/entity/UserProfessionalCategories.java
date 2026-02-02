package NytePulse.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "User_Professional_Categories")
@Data
public class UserProfessionalCategories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long categoryId;

    private String otherCategory;
}
