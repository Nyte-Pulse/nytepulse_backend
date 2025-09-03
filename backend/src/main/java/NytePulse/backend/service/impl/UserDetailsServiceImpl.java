package NytePulse.backend.service.impl;

import NytePulse.backend.dto.UserDetailsDto;
import NytePulse.backend.dto.UserDetailsUpdateRequest;
import NytePulse.backend.entity.UserDetails;
import NytePulse.backend.repository.UserDetailsRepository;
import NytePulse.backend.service.centralServices.UserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;


@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private static final ZoneId SRI_LANKA_ZONE = ZoneId.of("Asia/Colombo");

    @Override
    public ResponseEntity<?> updateUserDetails(String userId, UserDetailsDto userDetailsDto) {
        try {
            logger.info("Updating user details for userId: {}", userId);

            if (userId == null || userId.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid userId for updating user details");
                errorResponse.put("message", "UserId cannot be null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (userDetailsDto == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid request");
                errorResponse.put("message", "Request body cannot be null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            UserDetails userDetails = userDetailsRepository.findByUserId(userId);

            if (userDetails == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User details not found");
                errorResponse.put("userId", userId);
                errorResponse.put("message", "No user details found for the provided userId");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            if (userDetailsDto.getBio() != null) {
                userDetails.setBio(userDetailsDto.getBio());
            }

            if (userDetailsDto.getGender() != null) {
                userDetails.setGender(userDetailsDto.getGender());
            }

            if (userDetailsDto.getBirthday() != null) {
                userDetails.setBirthday(userDetailsDto.getBirthday());
            }

            if (userDetailsDto.getProfilePictureId() != null) {
                userDetails.setProfilePictureId(userDetailsDto.getProfilePictureId());
            }

            // Save updated user details
            UserDetails updatedUserDetails = userDetailsRepository.save(userDetails);

            logger.info("User details updated successfully for userId: {}", userId);

            // Create response with updated fields
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User details updated successfully");
            response.put("userId", userId);
            response.put("bio", updatedUserDetails.getBio());
            response.put("gender", updatedUserDetails.getGender());
            response.put("birthday", updatedUserDetails.getBirthday());
            response.put("profile_picture_id", updatedUserDetails.getProfilePictureId());
            response.put("updated_at", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating user details for userId: {}", userId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user details: " + e.getMessage());
        }
    }
}
