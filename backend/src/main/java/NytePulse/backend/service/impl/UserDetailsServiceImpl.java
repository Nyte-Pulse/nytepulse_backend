package NytePulse.backend.service.impl;

import NytePulse.backend.dto.UserDetailsDto;
import NytePulse.backend.entity.*;
import NytePulse.backend.repository.ClubDetailsRepository;
import NytePulse.backend.repository.UserDetailsRepository;
import NytePulse.backend.repository.UserRelationshipRepository;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.service.centralServices.UserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private ClubDetailsRepository clubDetailsRepository;;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRelationshipRepository userRelationshipRepository;

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
            if (userDetailsDto.getName() != null) {
                userDetails.setName(userDetailsDto.getName());
            }

            userDetails.setIsPrivate(userDetailsDto.getIsPrivate());

            UserDetails updatedUserDetails = userDetailsRepository.save(userDetails);

            logger.info("User details updated successfully for userId: {}", userId);

            // Create response with updated fields
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User details updated successfully");
            response.put("userId", userId);
            response.put("bio", updatedUserDetails.getBio());
            response.put("gender", updatedUserDetails.getGender());
            response.put("name", updatedUserDetails.getName());
            response.put("birthday", updatedUserDetails.getBirthday());
            response.put("profile_picture_id", updatedUserDetails.getProfilePictureId());
            response.put("updated_at", LocalDateTime.now(SRI_LANKA_ZONE));
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating user details for userId: {}", userId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user details: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> setAccountPrivateOrPublic(String userId, Boolean isPrivate) {
        try{
        UserDetails userDetails = userDetailsRepository.findByUserId(userId);
        if (userDetails == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "User details not found");
            errorResponse.put("userId", userId);
            errorResponse.put("message", "No user details found for the provided userId");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        userDetails.setIsPrivate(isPrivate);
        userDetailsRepository.save(userDetails);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Account status update success");
    } catch (Exception e) {
        logger.error("Error updating user details for userId: {}", userId, e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating user details: " + e.getMessage());
    }
    }

    @Override
    public ResponseEntity<?> getAccountNameByEmail(String email) {
        try {
            UserDetails userDetails = userDetailsRepository.findByEmail(email);
            if (userDetails == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User details not found");
                errorResponse.put("email", email);
                errorResponse.put("message", "No user details found for the provided email");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userDetails.getUserId());
            response.put("accountName", userDetails.getName());
            response.put("profile Picture Url", userDetails.getProfilePicture());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving account name for email: {}", email, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving account name: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> searchAccountByName(String name, Pageable pageable) {
        try {
            Page<UserDetails> userDetailsPage = userDetailsRepository
                    .findByNameContainingIgnoreCase(name, pageable);

            Page<ClubDetails> clubDetailsPage = clubDetailsRepository
                    .findByNameContainingIgnoreCase(name, pageable);

            List<Map<String, Object>> userResults = userDetailsPage.getContent()
                    .stream()
                    .map(user -> {
                        Map<String, Object> userMap = new HashMap<>();

                        Optional<User> userOptional = userRepository.findByUsername(user.getUsername());

                        if (userOptional.isPresent()) {
                            userMap.put("id", userOptional.get().getId());
                        } else {
                            userMap.put("id", null);
                            logger.warn("User not found for username: {}", user.getUsername());
                        }

                        userMap.put("userId", user.getUserId());
                        userMap.put("accountName", user.getName());
                        userMap.put("profilePictureUrl", user.getProfilePicture());
                        userMap.put("username", user.getUsername());

                        return userMap;
                    })
                    .collect(Collectors.toList());

            List<Map<String, Object>> clubResults = clubDetailsPage.getContent()
                    .stream()
                    .map(club -> {
                        Map<String, Object> clubMap = new HashMap<>();
                        Optional<User> userOptional = userRepository.findByUsername(club.getUsername());

                        if (userOptional.isPresent()) {
                            clubMap.put("id", userOptional.get().getId());
                        } else {
                            clubMap.put("id", null);
                            logger.warn("User not found for username: {}", club.getUsername());
                        }
                        clubMap.put("userId", club.getUserId());
                        clubMap.put("accountName", club.getName());
                        clubMap.put("username", club.getUsername());
                        clubMap.put("profilePictureUrl", club.getProfilePictureId());
                        return clubMap;
                    })
                    .collect(Collectors.toList());

            List<Map<String, Object>> combinedResults = new ArrayList<>();
            combinedResults.addAll(userResults);
            combinedResults.addAll(clubResults);

            long totalItems = userDetailsPage.getTotalElements() + clubDetailsPage.getTotalElements();
            int totalPages = Math.max(userDetailsPage.getTotalPages(), clubDetailsPage.getTotalPages());

            Map<String, Object> response = new HashMap<>();
            response.put("results", combinedResults);
            response.put("currentPage", pageable.getPageNumber());
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("userCount", userDetailsPage.getTotalElements());
            response.put("businessCount", clubDetailsPage.getTotalElements());
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching accounts by name: {}", name, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to search accounts");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }


    @Override
    public ResponseEntity<?> getAllBusinessAccount(){
        try{
            List<UserDetails> userDetails=userDetailsRepository.findByAccountType("BUSINESS");
            if(userDetails.size()==0){

                Map<String, Object> response = new HashMap<>();
                response.put("message", "No Data");
                response.put("status", HttpStatus.NOT_FOUND.value());
                return ResponseEntity.ok(response);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("userDetails", userDetails);
            response.put("message", "Data fetched successfully");
            response.put("status", HttpStatus.OK.value());
            return ResponseEntity.ok(response);

        }catch (Exception e) {
            logger.error("Error get all BusinessAccount: {}", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching accounts: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> searchFollowerAccountByName(Long userId, String searchTerm, Pageable pageable) {
        try {
            // Trim search term
            String trimmedSearch = searchTerm != null ? searchTerm.trim() : "";

            if (trimmedSearch.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Search term cannot be empty",
                        "status", 400
                ));
            }

            List<String> followerUserIds = userRelationshipRepository
                    .findFollowerUserIdsByFollowingId(userId);

            // Check if user has any followers
            if (followerUserIds.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "results", List.of(),
                        "currentPage", pageable.getPageNumber(),
                        "totalItems", 0,
                        "totalPages", 0,
                        "message", "No followers found",
                        "status", 200
                ));
            }

            Page<UserDetails> userDetailsPage = userDetailsRepository
                    .searchByNameInUserIds(followerUserIds, trimmedSearch, pageable);

            List<Map<String, Object>> userResults = userDetailsPage.getContent()
                    .stream()
                    .map(userDetails -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("userId", userDetails.getUserId());
                        userMap.put("accountName", userDetails.getName());
                        userMap.put("username", userDetails.getUsername());
                        userMap.put("profilePictureUrl", userDetails.getProfilePicture());
                        userMap.put("bio", userDetails.getBio());
                        userMap.put("isPrivate", userDetails.getIsPrivate());
                        return userMap;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("results", userResults);
            response.put("currentPage", userDetailsPage.getNumber());
            response.put("totalItems", userDetailsPage.getTotalElements());
            response.put("totalPages", userDetailsPage.getTotalPages());
            response.put("hasNext", userDetailsPage.hasNext());
            response.put("hasPrevious", userDetailsPage.hasPrevious());
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching follower accounts: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Failed to search follower accounts",
                    "message", e.getMessage(),
                    "status", 500
            ));
        }
    }





}
