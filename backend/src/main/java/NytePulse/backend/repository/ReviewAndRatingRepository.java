package NytePulse.backend.repository;

import NytePulse.backend.entity.ReviewAndRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewAndRatingRepository extends JpaRepository<ReviewAndRating, Long> {

    // Find review by user ID
    List<ReviewAndRating> findByUserId(String userId);

    // Find review by user and club
    Optional<ReviewAndRating> findByUserIdAndIdOfReviewer(String userId, String idOfReviewer);

    // Find reviews by club ID
//    List<ReviewAndRating> findByClubId(String userId);

    // Count reviews by club
    long countByUserId(String userId);

    // Custom query for average rating
    @Query("SELECT AVG(r.rating) FROM ReviewAndRating r WHERE r.userId = :clubId")
    Double getAverageRatingByClubId(@Param("clubId") String userId);
}
