package NytePulse.backend.service.impl;

import NytePulse.backend.dto.ClubDetailsDto;
import NytePulse.backend.entity.ClubDetails;
import NytePulse.backend.entity.ReviewAndRating;
import NytePulse.backend.repository.ClubDetailsRepository;
import NytePulse.backend.repository.ReviewAndRatingRepository;
import NytePulse.backend.service.centralServices.ClubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClubServiceImpl implements ClubService {

    @Autowired
    private ClubDetailsRepository clubDetailsRepository;

    @Autowired
    private ReviewAndRatingRepository reviewRepository;

    private static final Logger logger = LoggerFactory.getLogger(ClubServiceImpl.class);
    private static final ZoneId SRI_LANKA_ZONE = ZoneId.of("Asia/Colombo");

    @Override
    public ResponseEntity<?> updateClubDetails(String userId, ClubDetailsDto request) {
        try {
            logger.info("Updating club details for userId: {}", userId);

            if (userId == null || userId.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid userId for updating club details ");
                errorResponse.put("message", "UserId cannot be null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (request == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid request");
                errorResponse.put("message", "Request body cannot be null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            ClubDetails clubDetails = clubDetailsRepository.findByUserId(userId);
            if (clubDetails == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Club details not found");
                errorResponse.put("userId", userId);
                errorResponse.put("message", "No club details found for the provided userId");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            if (request.getBio() != null) {
                clubDetails.setBio(request.getBio());
            }
            if (request.getContactPhone() != null) {
                clubDetails.setContactPhone(request.getContactPhone());
            }
            if (request.getName() != null) {
                clubDetails.setName(request.getName());
            }
            if (request.getFollowersCount() != null) {
                clubDetails.setFollowersCount(request.getFollowersCount());
            }
            if (request.getRatingAvg() != 0) {
                clubDetails.setRatingAvg(request.getRatingAvg());
            }
            if (request.getEventsPublishedCount() != null) {
                clubDetails.setEventsPublishedCount(request.getEventsPublishedCount());
            }
            if (request.getProfilePictureId() != null) {
                clubDetails.setProfilePictureId(request.getProfilePictureId());
            }

            ClubDetails updated = clubDetailsRepository.save(clubDetails);
            logger.info("Successfully updated club details for userId: {}", userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Club details updated successfully");
            response.put("userId", userId);
            response.put("bio", updated.getBio());
            response.put("contactPhone", updated.getContactPhone());
            response.put("name", updated.getName());
            response.put("followersCount", updated.getFollowersCount());
            response.put("ratingAvg", updated.getRatingAvg());
            response.put("eventsPublishedCount", updated.getEventsPublishedCount());
            response.put("profile_picture_id", updated.getProfilePictureId());
            response.put("updated_at", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        }  catch (Exception e) {
            logger.error("Error updating club details for userId: {}", userId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating club details: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getClubDetailsByUserId(String userId) {
        try {
            logger.info("Fetching club details for userId: {}", userId);

            if (userId == null || userId.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid userId for fetching club details");
                errorResponse.put("message", "UserId cannot be null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }


            ClubDetails clubDetails = clubDetailsRepository.findByUserId(userId);

            if (clubDetails == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Club details not found!");
                errorResponse.put("userId", userId);
                errorResponse.put("message", "No club details found for the provided userId");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }


            List<ReviewAndRating> reviews = reviewRepository.findByUserId(userId);
            Double averageRating = reviewRepository.getAverageRatingByClubId(userId);
            long totalReviews = reviewRepository.countByUserId(userId);


            Map<String, Object> response = new HashMap<>();
            response.put("userId", clubDetails.getUserId());
            response.put("bio", clubDetails.getBio());
            response.put("contactPhone", clubDetails.getContactPhone());
            response.put("name", clubDetails.getName());
            response.put("followersCount", clubDetails.getFollowersCount());
            response.put("eventsPublishedCount", clubDetails.getEventsPublishedCount());
            response.put("profile_picture_id", clubDetails.getProfilePictureId());
            response.put("updated_at", LocalDateTime.now(SRI_LANKA_ZONE));
            response.put("clubId", userId);
            response.put("reviews", reviews);
            response.put("averageRating", averageRating != null ? averageRating : 0.0);
            response.put("totalReviews", totalReviews);
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            logger.info("Successfully fetched club details for userId: {}", userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching club details for userId: {}", userId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching club details: " + e.getMessage());
        }
    }

}
