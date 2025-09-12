package NytePulse.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_and_rating")
public class ReviewAndRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Add back the userId field - it's needed for your repository
    @Column(name = "user_id")
    private String userId;

    @Column(name = "review")
    private String review; // Changed to String for text reviews

    @Column(name = "rating")
    private int rating;

    @Column(name = "name_of_reviewer")
    private String nameOfReviewer;


    @Column(name = "id_of_reviewer")
    private String idOfReviewer;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    // Default constructor
    public ReviewAndRating() {
    }

    // Getters and Setters
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

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getNameOfReviewer() {
        return nameOfReviewer;
    }

    public void setNameOfReviewer(String nameOfReviewer) {
        this.nameOfReviewer = nameOfReviewer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public String getIdOfReviewer() {
        return idOfReviewer;
    }
    public void setIdOfReviewer(String idOfReviewer) {
        this.idOfReviewer = idOfReviewer;
    }
}
