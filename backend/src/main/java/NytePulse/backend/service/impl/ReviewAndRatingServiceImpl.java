package NytePulse.backend.service.impl;

import NytePulse.backend.dto.ReviewAndRatingDto;
import NytePulse.backend.entity.ClubDetails;
import NytePulse.backend.entity.ReviewAndRating;
import NytePulse.backend.repository.ClubDetailsRepository;
import NytePulse.backend.repository.ReviewAndRatingRepository;
import NytePulse.backend.service.centralServices.ReviewAndRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class ReviewAndRatingServiceImpl implements ReviewAndRatingService {


    @Autowired
    private ReviewAndRatingRepository reviewRepository;

    @Autowired
    private ClubDetailsRepository clubRepository;

    private static final ZoneId SRI_LANKA_ZONE = ZoneId.of("Asia/Colombo");

    @Override
    public ResponseEntity<?> addReview(String clubId,ReviewAndRatingDto reviewDto) {
        try {

            ClubDetails club = clubRepository.findByUserId(clubId);

            //give code for club is exists
            if (club == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Club not found");
                errorResponse.put("message", "The specified club does not exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Check if user already reviewed this club
            Optional<ReviewAndRating> existingReview =
                    reviewRepository.findByUserIdAndIdOfReviewer(
                            clubId,
                            reviewDto.getIdOfReviewer()
                    );

            if (existingReview.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Review already exists");
                errorResponse.put("message", "You have already reviewed this club");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            // Create new review
            ReviewAndRating review = new ReviewAndRating();
            review.setUserId(clubId);
            review.setReview(reviewDto.getReview());
            review.setRating(reviewDto.getRating());
            review.setNameOfReviewer(reviewDto.getNameOfReviewer());
            review.setIdOfReviewer(reviewDto.getIdOfReviewer());
            review.setCreatedAt(LocalDateTime.now(SRI_LANKA_ZONE));

            ReviewAndRating savedReview = reviewRepository.save(review);


            Map<String, Object> response = new HashMap<>();
            response.put("message", "Review added successfully");
            response.put("reviewId", savedReview.getId());
            response.put("rating", savedReview.getRating());
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to add review");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @Override
    public ResponseEntity<?> getClubReviews(String clubId) {
        try {
            List<ReviewAndRating> reviews = reviewRepository.findByUserId(clubId);
            Double averageRating = reviewRepository.getAverageRatingByClubId(clubId);
            long totalReviews = reviewRepository.countByUserId(clubId);


            Map<String, Object> response = new HashMap<>();
            response.put("clubId", clubId);
            response.put("reviews", reviews);
            response.put("averageRating", averageRating != null ? averageRating : 0.0);
            response.put("totalReviews", totalReviews);
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve reviews");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
