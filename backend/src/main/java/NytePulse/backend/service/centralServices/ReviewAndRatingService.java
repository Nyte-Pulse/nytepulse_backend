package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.ReviewAndRatingDto;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

public interface ReviewAndRatingService {
    ResponseEntity<?> addReview(String clubId, ReviewAndRatingDto reviewDto);

    ResponseEntity<?> getClubReviews(String clubId);

}
