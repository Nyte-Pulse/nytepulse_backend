package NytePulse.backend.service.impl;

import NytePulse.backend.auth.RegisterRequest;
import NytePulse.backend.dto.BunnyNetUploadResult;
import NytePulse.backend.dto.FeedbackRequest;
import NytePulse.backend.dto.FeedbackResponse;
import NytePulse.backend.entity.*;
import NytePulse.backend.enums.NotificationType;
import NytePulse.backend.repository.*;
import NytePulse.backend.service.BunnyNetService;
import NytePulse.backend.service.NotificationService;
import NytePulse.backend.service.UserSettingsService;
import NytePulse.backend.service.centralServices.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRelationshipRepository relationshipRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserSettingsService userSettingsService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ClubDetailsRepository clubDetailsRepository;

    @Autowired
    private BunnyNetService bunnyNetService;


    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final ZoneId SRI_LANKA_ZONE = ZoneId.of("Asia/Colombo");

    private String generateUserId(String accountType) {
        String prefix = "PS";
        if ("BUSINESS".equalsIgnoreCase(accountType)) {
            prefix = "BS";
        }

        // Fetch last user by accountType ordered by userId descending
        User lastUser = userRepository.findTopByAccountTypeOrderByUserIdDesc(accountType);
        String lastUserId = (lastUser != null) ? lastUser.getUserId() : null;

        if (lastUserId == null) {
            return prefix + "0000001";
        }

        try {
            String numberPart = lastUserId.substring(2); // Extract number part (assumes format XX00000)
            int number = Integer.parseInt(numberPart);
            number++;
            return String.format(prefix + "%07d", number);
        } catch (Exception e) {
            // Fallback if parsing fails
            logger.warn("Failed to parse userId: {}, using default", lastUserId);
            return prefix + "0000001";
        }
    }

    @Override
    public ResponseEntity<?> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Email is already taken!");
        }

        User user = new User(request.getUsername(), request.getEmail(), passwordEncoder.encode(request.getPassword()));
        user.setAccountType(request.getAccountType());

        String userId = generateUserId(request.getAccountType());
        System.out.println("Generated User ID: " + userId);
        user.setUserId(userId);
        LocalDateTime sriLankanTime = LocalDateTime.now(SRI_LANKA_ZONE);
        user.setCreatedAt(sriLankanTime);

        Set<Role> roles = new HashSet<>();

        Role.ERole roleEnum;
        try {
            roleEnum = Role.ERole.valueOf("ROLE_" + request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid role: " + request.getRole());
        }

        Role userRole = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);

        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        logger.info("User details saved successfully: {}", savedUser);

        UserDetails userDetails = new UserDetails(
                savedUser.getUserId(),
                request.getEmail(),
                request.getUsername(),
                request.getName() != null ? request.getName() : request.getUsername(),
                request.getAccountType()
        );

        if ("PERSONAL".equals(request.getAccountType())) {
            UserDetails savedUserDetails = userDetailsRepository.save(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "user details saved successfully");
            response.put("userId", savedUser.getUserId());
            response.put("username", savedUserDetails.getUsername());
            response.put("email", savedUserDetails.getEmail());
            response.put("updated_at", LocalDateTime.now(SRI_LANKA_ZONE));
            logger.info("User and UserDetails saved successfully: {}", savedUserDetails);


        } else if ("BUSINESS".equals(request.getAccountType())) {

            ClubDetails clubDetails = new ClubDetails(
                    savedUser.getUserId(),
                    request.getEmail(),
                    request.getUsername(),
                    request.getName() != null ? request.getName() : request.getName(),
                    request.getAccountType()
            );

            ClubDetails savedClubDetails = clubDetailsRepository.save(clubDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Club details saved successfully");
            response.put("bio", savedUser.getUserId());
            response.put("username", savedClubDetails.getUsername());
            response.put("email", savedClubDetails.getEmail());
            response.put("updated_at", LocalDateTime.now(SRI_LANKA_ZONE));
            logger.info("ClubDetails saved successfully: {}", savedClubDetails);
        }
        userSettingsService.createDefaultSettings(savedUser.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("name", request.getName() != null ? request.getName() : request.getUsername());
        response.put("message", "Account created successfully!");
        response.put("user", savedUser);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> followUser(String followerUserId, String followingUserId) {
        try {
            if (followerUserId.equals(followingUserId)) {
                throw new IllegalArgumentException("User cannot follow themselves");
            }

            if (relationshipRepository.isFollowing(followerUserId, followingUserId)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Already following user: " + followingUserId);

            }

            User follower = userRepository.findByUserId(followerUserId);

            User following = userRepository.findByUserId(followingUserId);

            UserRelationship relationship = new UserRelationship(follower, following);
            relationshipRepository.save(relationship);

          if(followerUserId.startsWith("BS")){
                ClubDetails followerDetails = clubDetailsRepository.findByUserId(followerUserId);

                String message = followerDetails.getName() + " started following you" + " profilePicture :" + followerDetails.getProfilePicture();
                notificationService.createNotification(
                        following.getId(),           // Recipient (the person being followed)
                        follower.getId(),            // Actor (the person who followed)
                        NotificationType.NEW_FOLLOWER,
                        message,
                        follower.getId(),            // Reference to follower
                        "USER"                       // Reference type
                );
            } else {
                UserDetails followerDetails = userDetailsRepository.findByUserId(followerUserId);

                String message = followerDetails.getName() + " started following you" + " profilePicture :" + followerDetails.getProfilePicture();
                notificationService.createNotification(
                        following.getId(),           // Recipient (the person being followed)
                        follower.getId(),            // Actor (the person who followed)
                        NotificationType.NEW_FOLLOWER,
                        message,
                        follower.getId(),            // Reference to follower
                        "USER"                       // Reference type
                );
          }


            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully followed user: " + followingUserId);
            response.put("status", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error while following user: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error while following user: " + followingUserId);
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.ok(errorResponse);
        }

    }

    @Override
    public ResponseEntity<?> blockUser(String blockerUserId, String blockedUserId) {
        try {
            if (blockerUserId.equals(blockedUserId)) {
                throw new IllegalArgumentException("User cannot block themselves");
            }

            // 1. Fetch Users (Assuming your repo returns User or null)
            User blocker = userRepository.findByUserId(blockerUserId);
            User blocked = userRepository.findByUserId(blockedUserId);

            if (blocker == null || blocked == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            // 2. Check if a relationship already exists (to avoid unique constraint errors)
            Optional<UserRelationship> existingRelationship = relationshipRepository
                    .findByFollowerAndFollowing(blocker, blocked);

            UserRelationship relationship;

            if (existingRelationship.isPresent()) {
                // CASE A: Update existing relationship (e.g., was 'FOLLOWING', now 'BLOCKED')
                relationship = existingRelationship.get();
                relationship.setRelationshipType(RelationshipType.BLOCKED);
                relationship.setCreatedAt(LocalDateTime.now()); // Update timestamp
            } else {
                // CASE B: Create new relationship
                relationship = new UserRelationship(blocker, blocked);
                relationship.setRelationshipType(RelationshipType.BLOCKED); // <--- IMPORTANT
            }

            relationshipRepository.save(relationship);

            // 3. (Optional but Recommended) Force unfollow in the reverse direction
            // If I block you, you shouldn't be following me anymore.
            Optional<UserRelationship> reverseRelationship = relationshipRepository
                    .findByFollowerAndFollowing(blocked, blocker);

            reverseRelationship.ifPresent(rel -> relationshipRepository.delete(rel));

            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully blocked user: " + blockedUserId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error while blocking user: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while trying to block the user.");
        }
    }


    @Override
    public ResponseEntity<?> getFollowers(String profileOwnerId, String currentLoginUserId, int page, int size) {
        try {

            Pageable pageable = PageRequest.of(page, size);
            Page<User> followersPage = relationshipRepository.getFollowers(profileOwnerId, pageable);

            if (followersPage.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("followers", Collections.emptyList());
                response.put("totalElements", 0);
                response.put("totalPages", 0);
                response.put("currentPage", page);
                response.put("status", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            }

            List<User> followersList = followersPage.getContent();

            Set<String> personalUserIds = new HashSet<>();
            Set<String> businessUserIds = new HashSet<>();

            List<Long> targetLongIds = new ArrayList<>();

            followersList.forEach(user -> {
                if (user.getUserId().startsWith("BS")) {
                    businessUserIds.add(user.getUserId());
                } else {
                    personalUserIds.add(user.getUserId());
                }

                // B. Collect Long IDs for "Am I Following?" check
                if (user.getId() != null) {
                    targetLongIds.add(user.getId());
                }
            });

            List<UserDetails> userDetailsList = personalUserIds.isEmpty() ?
                    Collections.emptyList() :
                    userDetailsRepository.findByUserIdIn(new ArrayList<>(personalUserIds));

            List<ClubDetails> clubDetailsList = businessUserIds.isEmpty() ?
                    Collections.emptyList() :
                    clubDetailsRepository.findByUserIdIn(new ArrayList<>(businessUserIds));

            Map<String, UserDetails> userDetailsMap = new HashMap<>();
            userDetailsList.forEach(ud -> userDetailsMap.putIfAbsent(ud.getUserId(), ud));

            Map<String, ClubDetails> clubDetailsMap = new HashMap<>();
            clubDetailsList.forEach(cd -> clubDetailsMap.putIfAbsent(cd.getUserId(), cd));

            Set<Long> followingIdsSet = new HashSet<>();

            Long currentUserIdLongVal = null;

            if (currentLoginUserId != null && !targetLongIds.isEmpty()) {
                try {
                    Optional<User> currentUserOpt = userRepository.findByEmail(currentLoginUserId);

                    if (currentUserOpt.isPresent()) {
                        currentUserIdLongVal = currentUserOpt.get().getId();

                        followingIdsSet = relationshipRepository
                                .findFollowingIdsByFollowerAndTargets(currentUserIdLongVal, targetLongIds);
                    }

                } catch (Exception e) {
                    logger.warn("Error resolving Current User ID for relationship check: {}", e.getMessage());
                }
            }

            List<Map<String, Object>> followerList = new ArrayList<>();

            for (User follower : followersList) {
                Map<String, Object> followerInfo = new HashMap<>();
                followerInfo.put("userId", follower.getUserId());
                followerInfo.put("username", follower.getUsername());
                followerInfo.put("email", follower.getEmail());

                if (follower.getUserId().startsWith("BS")) {
                    ClubDetails clubDetails = clubDetailsMap.get(follower.getUserId());
                    if (clubDetails != null) {
                        followerInfo.put("name", clubDetails.getName());
                        followerInfo.put("profilePicture", clubDetails.getProfilePicture());
                        followerInfo.put("accountType", "BUSINESS");
                    }
                } else {
                    UserDetails userDetails = userDetailsMap.get(follower.getUserId());
                    if (userDetails != null) {
                        followerInfo.put("name", userDetails.getName());
                        followerInfo.put("profilePicture", userDetails.getProfilePicture());
                        followerInfo.put("accountType", "PERSONAL");
                    }
                }

                boolean isFollowedByMe = false;

                if (follower.getId() != null) {
                    isFollowedByMe = followingIdsSet.contains(follower.getId());
                }

                if (currentUserIdLongVal != null && currentUserIdLongVal.equals(follower.getId())) {
                    isFollowedByMe = false;
                } else if (currentLoginUserId != null && currentLoginUserId.equals(follower.getEmail())) {
                    isFollowedByMe = false;
                }

                followerInfo.put("isFollowedByCurrentUser", isFollowedByMe);

                followerList.add(followerInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("count", followerList.size());
            response.put("followers", followerList);
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error while fetching followers: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while trying to fetch followers.");
        }
    }


    @Override
    public ResponseEntity<?> getFollowing(String profileOwnerId, String currentLoginUserId, int page, int size) {
        try {

            Pageable pageable = PageRequest.of(page, size);
            Page<User> followingPage = relationshipRepository.getFollowing(profileOwnerId, pageable);

            if (followingPage.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("following", Collections.emptyList());
                response.put("totalElements", 0);
                response.put("totalPages", 0);
                response.put("currentPage", page);
                response.put("status", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            }

            List<User> followingList = followingPage.getContent();

            Set<String> personalUserIds = new HashSet<>();
            Set<String> businessUserIds = new HashSet<>();

            List<Long> targetLongIds = new ArrayList<>();

            followingList.forEach(user -> {
                if (user.getUserId().startsWith("BS")) {
                    businessUserIds.add(user.getUserId());
                } else {
                    personalUserIds.add(user.getUserId());
                }

                if (user.getId() != null) {
                    targetLongIds.add(user.getId());
                }
            });

            List<UserDetails> userDetailsList = personalUserIds.isEmpty() ? Collections.emptyList() :
                    userDetailsRepository.findByUserIdIn(new ArrayList<>(personalUserIds));

            List<ClubDetails> clubDetailsList = businessUserIds.isEmpty() ? Collections.emptyList() :
                    clubDetailsRepository.findByUserIdIn(new ArrayList<>(businessUserIds));

            Map<String, UserDetails> userDetailsMap = new HashMap<>();
            userDetailsList.forEach(ud -> userDetailsMap.putIfAbsent(ud.getUserId(), ud));

            Map<String, ClubDetails> clubDetailsMap = new HashMap<>();
            clubDetailsList.forEach(cd -> clubDetailsMap.putIfAbsent(cd.getUserId(), cd));


            Set<Long> followingIdsSet = new HashSet<>();

            if (currentLoginUserId != null && !targetLongIds.isEmpty()) {
                try {
                    Optional<User> currentUserIdLong = userRepository.findByEmail(currentLoginUserId);

                    followingIdsSet = relationshipRepository
                            .findFollowingIdsByFollowerAndTargets(currentUserIdLong.get().getId(), targetLongIds);

                } catch (NumberFormatException e) {
                    logger.warn("Current User ID '{}' could not be parsed to Long for relationship check.", currentLoginUserId);
                }
            }

            List<Map<String, Object>> responseList = new ArrayList<>();

            for (User followee : followingList) {
                Map<String, Object> followeeInfo = new HashMap<>();
                followeeInfo.put("userId", followee.getUserId()); // String ID (e.g., "BS101")
                followeeInfo.put("username", followee.getUsername());
                followeeInfo.put("email", followee.getEmail());

                if (followee.getUserId().startsWith("BS")) {
                    ClubDetails club = clubDetailsMap.get(followee.getUserId());
                    if (club != null) {
                        followeeInfo.put("name", club.getName());
                        followeeInfo.put("profilePicture", club.getProfilePicture());
                        followeeInfo.put("accountType", "BUSINESS");
                    }
                } else {
                    UserDetails details = userDetailsMap.get(followee.getUserId());
                    if (details != null) {
                        followeeInfo.put("name", details.getName());
                        followeeInfo.put("profilePicture", details.getProfilePicture());
                        followeeInfo.put("accountType", "PERSONAL");
                    }
                }

                boolean isFollowedByMe = false;

                if (followee.getId() != null) {
                    isFollowedByMe = followingIdsSet.contains(followee.getId());
                }

                if (followee.getUserId().equals(currentLoginUserId)) {
                    isFollowedByMe = false;
                }

                followeeInfo.put("isFollower", isFollowedByMe);

                responseList.add(followeeInfo);
            }

            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("count", responseList.size());
            finalResponse.put("following", responseList);
            finalResponse.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(finalResponse);

        } catch (Exception e) {
            logger.error("Error fetching following list: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching following.");
        }
    }


    @Override
    public ResponseEntity<?> unfollowUser(String userId, String followingUserId) {
        try {
            if (userId.equals(followingUserId)) {
                throw new IllegalArgumentException("User cannot unfollow themselves");
            }

            Optional<UserRelationship> relationshipOpt = relationshipRepository.findByFollowerUserIdAndFollowingUserId(userId, followingUserId);
            if (relationshipOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Not following user: " + followingUserId);
            }

            relationshipRepository.delete(relationshipOpt.get());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully unfollowed user: " + followingUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error while unfollowing user: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while trying to unfollow the user.");
        }
    }

    @Override
    public ResponseEntity<?> unblock(String blockerUserId, String blockedUserId) { // Renamed arguments for clarity
        try {
            if (blockerUserId.equals(blockedUserId)) {
                throw new IllegalArgumentException("User cannot unblock themselves");
            }

            Optional<UserRelationship> relationshipOpt = relationshipRepository
                    .findByFollowerUserIdAndFollowingUserId(blockerUserId, blockedUserId);

            if (relationshipOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("No relationship found with user: " + blockedUserId);
            }

            UserRelationship relationship = relationshipOpt.get();

            if (relationship.getRelationshipType() != RelationshipType.BLOCKED) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("User is not blocked, so they cannot be unblocked.");
            }

            relationshipRepository.delete(relationship);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully unblocked user: " + blockedUserId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error while unblocking user: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while trying to unblock the user.");
        }
    }

    @Override
    public Boolean isFollowing(String followerUserId, String followingUserId) {
        return relationshipRepository.isFollowing(followerUserId, followingUserId);
    }

    @Override
    public boolean isBlocked(String targetUserId, String userId) {
        return relationshipRepository.isBlocked(userId, targetUserId);
    }

    public Boolean isFollowers(String followerUserId, String followingUserId) {
        return relationshipRepository.isFollowers(followingUserId, followerUserId);
    }

    @Override
    public ResponseEntity<?> getFollowersCount(String userId) {
        try {
            long count = relationshipRepository.countFollowers(userId);
            Map<String, Long> response = new HashMap<>();
            response.put("followersCount", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error while counting followers: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while trying to count followers.");
        }
    }

    @Override
    public ResponseEntity<?> acceptOrRejectedFollowRequest(String userId, String followingUserId,String status){

        try {
            Optional<UserRelationship> relationshipOpt = relationshipRepository.findByFollowerUserIdAndFollowingUserId(followingUserId, userId);
            if (relationshipOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("No follow request found from user: " + followingUserId);
            }

            UserRelationship relationship = relationshipOpt.get();

            if (!relationship.getRelationshipType().equals(RelationshipType.FOLLOW_REQUESTED)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("No pending follow request from user: " + followingUserId);
            }

            if(status.equalsIgnoreCase("ACCEPT")){
                relationship.setRelationshipType(RelationshipType.FOLLOWING);
                relationshipRepository.save(relationship);

                Map<String, String> response = new HashMap<>();
                response.put("message", "Follow request accepted from user: " + followingUserId);
                return ResponseEntity.ok(response);

            } else if(status.equalsIgnoreCase("REJECT")){
                relationshipRepository.delete(relationship);

                Map<String, String> response = new HashMap<>();
                response.put("message", "Follow request rejected from user: " + followingUserId);
                return ResponseEntity.ok(response);

            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Invalid status. Use 'ACCEPT' or 'REJECT'.");
            }

        } catch (Exception e) {
            logger.error("Error while processing follow request: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while trying to process the follow request.");
        }
    }


    @Override
    public ResponseEntity<?> sendFollowRequest(String userId, String followingUserId) {

        try {

            if (relationshipRepository.isFollowing(userId, followingUserId)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Already following user: " + followingUserId);

            }

            if(userDetailsRepository.findByUserId(followingUserId).getIsPrivate() == false){
                HashMap<String, Object> response = new HashMap<>();
                response.put("status", HttpStatus.BAD_REQUEST.value());
                response.put("message", "User account is not private, you can follow directly: " + followingUserId);
                return ResponseEntity.ok(response);

            }

            User follower = userRepository.findByUserId(userId);

            User following = userRepository.findByUserId(followingUserId);

            UserRelationship relationship = new UserRelationship(follower, following);
            relationship.setRelationshipType(RelationshipType.FOLLOW_REQUESTED);
            relationshipRepository.save(relationship);

            notificationService.createNotification(
                    following.getId(),           // Recipient (the person being followed)
                    follower.getId(),            // Actor (the person who followed)
                    NotificationType.FOLLOW_REQUEST,
                    "You have a new follow request",
                    follower.getId(),            // Reference to follower
                    "USER"                       // Reference type
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Follow request sent to user: " + followingUserId);
            response.put("status", HttpStatus.OK.value());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error while sending follow request: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while trying to send follow request.");
        }
    }

    @Override
    public ResponseEntity<?> getFollowingCount(String userId) {
        try {
            long count = relationshipRepository.countFollowing(userId);
            Map<String, Long> response = new HashMap<>();
            response.put("followingCount", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error while counting following: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while trying to count following.");
        }
    }

    @Override
    public ResponseEntity<?> getUserByUsername(String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("User not found with username: " + username);
            }

            User user = userOpt.get();
            String userId = user.getUserId();

            long countFollowing = relationshipRepository.countFollowing(userId);
            long countFollowers = relationshipRepository.countFollowers(userId);
            List<Post> posts = postRepository.findByUserOrderByCreatedAtDesc(user);

            Map<String, Object> response = new HashMap<>();
            response.put("followingCount", String.valueOf(countFollowing));
            response.put("followersCount", String.valueOf(countFollowers));
            response.put("id",user.getId());
            response.put("userId", userId);
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("postsCount", String.valueOf(posts.size()));
            response.put("accountType", user.getAccountType());

            if (userId.startsWith("PS")) {
                UserDetails userDetails = userDetailsRepository.findByUsername(username);
                response.put("name", userDetails.getName());
                response.put("birthDay", userDetails.getBirthday());
                response.put("gender", userDetails.getGender());
                response.put("profileImage", userDetails.getProfilePicture());
                response.put("profileImageFileName", userDetails.getProfilePictureFileName());
                response.put("bio", userDetails.getBio());
                response.put("isPrivate", userDetails.getIsPrivate());
            } else {
                ClubDetails clubDetails = clubDetailsRepository.findByUsername(username);
                response.put("profileImage", clubDetails.getProfilePicture());
                response.put("profileImageFileName", clubDetails.getProfilePictureFileName());
                response.put("name", clubDetails.getName());
                response.put("bio", clubDetails.getBio());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error while fetching user by username: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "An error occurred while trying to fetch the user");
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.ok(response);
        }
    }

    public ResponseEntity<?> checkUsernameAvailability(String username) {
        try {
            boolean exists = userRepository.existsByUsername(username);
            Map<String, Object> response = new HashMap<>();
            response.put("available", !exists);
            response.put("status", HttpStatus.OK.value());
            response.put("message", !exists ? "Username is available" : "Username already exists");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error while checking username availability: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while trying to check username availability.");
        }
    }

    @Override
    public ResponseEntity<?> uploadProfilePicture(MultipartFile file, String userId){

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File is empty"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File must be an image"));
            }

            // Validate file size (e.g., max 5MB)
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File size exceeds 5MB limit"));
            }

            logger.info("Uploading profile picture for user: {}", userId);

            // Upload to BunnyNet
            BunnyNetUploadResult result = bunnyNetService.uploadProfilePicture(file, userId);

            if(userId.startsWith("BS")){
                ClubDetails clubDetailsOpt = clubDetailsRepository.findByUserId(userId);

                if (clubDetailsOpt == null) {
                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body("Business Details not found for userId: " + userId);
                }
                clubDetailsOpt.setProfilePicture(result.getCdnUrl());
                clubDetailsOpt.setProfilePictureFileName(result.getFileName());
                clubDetailsRepository.save(clubDetailsOpt);
            }else {
                UserDetails userDetailsOpt = userDetailsRepository.findByUserId(userId);

                if (userDetailsOpt == null) {
                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body("UserDetails not found for userId: " + userId);
                }
                userDetailsOpt.setProfilePicture(result.getCdnUrl());
                userDetailsOpt.setProfilePictureFileName(result.getFileName());
                userDetailsRepository.save(userDetailsOpt);
            }



            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile picture uploaded successfully");
            response.put("fileName", result.getFileName());
            response.put("cdnUrl", result.getCdnUrl());
            response.put("status", HttpStatus.valueOf(200).toString());
            response.put("fileSize", result.getFileSize());

            return ResponseEntity.ok(response);


        } catch (Exception e) {
            logger.error("Error while uploading profile picture: {}", e.getMessage());
            Map<String, Object> erroresponse = new HashMap<>();
            erroresponse.put("message", "An error occurred while trying to upload the profile picture.");
            erroresponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity.ok(erroresponse);
        }

    }

    @Override
    public ResponseEntity<?> updateProfilePicture(MultipartFile file,String userId,String oldFileName){

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File is empty"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File must be an image"));
            }

            logger.info("Updating profile picture for user: {}", userId);

            BunnyNetUploadResult result = bunnyNetService.updateProfilePicture(file, userId, oldFileName);

            if(userId.startsWith("BS")){
                ClubDetails clubDetailsOpt = clubDetailsRepository.findByUserId(userId);

                if (clubDetailsOpt == null) {
                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body("Business Details not found for userId: " + userId);
                }
                clubDetailsOpt.setProfilePicture(result.getCdnUrl());
                clubDetailsRepository.save(clubDetailsOpt);
            }else{
                UserDetails userDetailsOpt = userDetailsRepository.findByUserId(userId);

                if (userDetailsOpt == null) {
                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body("UserDetails not found for userId: " + userId);
                }
                userDetailsOpt.setProfilePicture(result.getCdnUrl());
                userDetailsRepository.save(userDetailsOpt);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", HttpStatus.valueOf(200).toString());
            response.put("message", "Profile picture updated successfully");
            response.put("fileName", result.getFileName());
            response.put("cdnUrl", result.getCdnUrl());
            response.put("fileSize", result.getFileSize());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("Error updating profile picture: {}", e.getMessage());
            Map<String, Object> erroresponse = new HashMap<>();
            erroresponse.put("message", "Failed to update profile picture: "+ e.getMessage());
            erroresponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.ok(erroresponse);
        }

    }

    @Override
    public ResponseEntity<?> deleteProfilePicture(String fileName,String userId){
        try {
            UserDetails userDetailsOpt = userDetailsRepository.findByUserId(userId);

            if (userDetailsOpt == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("UserDetails not found for userId: " + userId);
            }
            userDetailsOpt.setProfilePictureFileName("Empty");
            userDetailsOpt.setProfilePicture("Empty");
            userDetailsRepository.save(userDetailsOpt);
            boolean deleted = bunnyNetService.deleteProfilePicture(fileName);

            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("status", HttpStatus.valueOf(200).toString());
                response.put("success", "true");
                response.put("message", "Profile picture deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Profile picture not found"));
            }

        } catch (Exception e) {
            logger.error("Error deleting profile picture: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete profile picture: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("success", "false");
        error.put("error", message);
        return error;
    }


    @Override
    public ResponseEntity<?> generateProfileShareLink(Long userId){
        try {
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("User not found with ID: " + userId);
            }

            User user = userOpt.get();
            String profileLink = "https://www.nytepulse.com/profile/" + user.getUsername();

            Map<String, Object> response = new HashMap<>();
            response.put("profileLink", profileLink);
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Profile share link generated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error generating profile share link: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while generating the profile share link.");
        }
    }

    @Override
    public ResponseEntity<?>  getMostFollowersCountUsers(){
        try {
            List<Object[]> topUsersData = relationshipRepository.findTopUsersByFollowersCount(PageRequest.of(0, 10));

            List<Map<String, Object>> topUsersList = new ArrayList<>();

            for (Object[] row : topUsersData) {
                Long userId = (Long) row[0];
                Long followersCount = (Long) row[1];

                Optional<User> user= userRepository.findById(userId);

                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", userId);
                userMap.put("followersCount", followersCount);


                if (user.get().getUserId().startsWith("BS")) {
                    ClubDetails clubDetails = clubDetailsRepository.findByUserId(user.get().getUserId());
                    if (clubDetails != null) {
                        userMap.put("name", clubDetails.getName());
                        userMap.put("profilePicture", clubDetails.getProfilePicture());
                        userMap.put("accountType", "BUSINESS");
                        userMap.put("username", clubDetails.getUsername());
                        userMap.put("userId", clubDetails.getUserId());

                    }
                } else {
                    UserDetails userDetails = userDetailsRepository.findByUserId(user.get().getUserId());
                    if (userDetails != null) {
                        userMap.put("name", userDetails.getName());
                        userMap.put("profilePicture", userDetails.getProfilePicture());
                        userMap.put("accountType", "PERSONAL");
                        userMap.put("username", userDetails.getUsername());
                        userMap.put("userId", userDetails.getUserId());
                    }
                }

                topUsersList.add(userMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("topUsers", topUsersList);
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Top users by followers count retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving top users by followers count: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving the top users by followers count.");
        }
    }

    @Override
    public ResponseEntity<?> getBlockListByUserId(Long userId){
        try {
            List<UserRelationship> blockedRelationships = relationshipRepository.findBlockedUsersByBlockerId(userId);

            List<Map<String, Object>> blockedUsersList = new ArrayList<>();

            for (UserRelationship relationship : blockedRelationships) {
                User blockedUser = relationship.getFollowing();
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", blockedUser.getUserId());
                userMap.put("username", blockedUser.getUsername());
                userMap.put("email", blockedUser.getEmail());

                if (blockedUser.getUserId().startsWith("BS")) {
                    ClubDetails clubDetails = clubDetailsRepository.findByUserId(blockedUser.getUserId());
                    if (clubDetails != null) {
                        userMap.put("name", clubDetails.getName());
                        userMap.put("profilePicture", clubDetails.getProfilePicture());
                        userMap.put("accountType", "BUSINESS");
                    }
                } else {
                    UserDetails userDetails = userDetailsRepository.findByUserId(blockedUser.getUserId());
                    if (userDetails != null) {
                        userMap.put("name", userDetails.getName());
                        userMap.put("profilePicture", userDetails.getProfilePicture());
                        userMap.put("accountType", "PERSONAL");
                    }
                }

                blockedUsersList.add(userMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("blockedUsers", blockedUsersList);
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Blocked users retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving blocked users: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving the blocked users.");
        }
    }

//    @Transactional
//    public void updateUserStatus(Long userId, boolean isOnline) {
//        User user = userRepository.findById(userId).orElse(null);
//        if (user != null) {
//            user.setOnline(isOnline); // You need this field in User Entity
//            if (!isOnline) {
//                user.setLastSeen(LocalDateTime.now());
//            }
//            userRepository.save(user);
//        }
//    }

    @Override
    public ResponseEntity<?> saveFeedback(FeedbackRequest request) {

        UserDetails userDetails = null;
        ClubDetails clubDetails = null;

        if (request.getUserId().startsWith("PS")) {
            userDetails = userDetailsRepository.findByUserId(request.getUserId());
        } else if (request.getUserId().startsWith("BS")) {
            clubDetails = clubDetailsRepository.findByUserId(request.getUserId());
        }

        FeedBack feedback = new FeedBack();
        feedback.setMessage(request.getMessage());
        feedback.setRating(request.getRating());

        if (userDetails != null) {
            feedback.setUserDetails(userDetails);
        } else if (clubDetails != null) {
            feedback.setClubDetails(clubDetails);
        } else {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid User/Club ID"));
        }

        feedbackRepository.save(feedback);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Feedback submitted successfully");
        return ResponseEntity.ok(response);
    }
    @Override
    public ResponseEntity<?> getAllFeedback(int page,int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<FeedBack> feedbacks = feedbackRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<FeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("feedbacks", feedbackResponses);
        return ResponseEntity.ok(response);
    }



    private FeedbackResponse mapToResponse(FeedBack feedback) {
        FeedbackResponse response = new FeedbackResponse();
        response.setFeedbackId(feedback.getId());
        response.setMessage(feedback.getMessage());
        response.setRating(feedback.getRating());
        response.setCreatedAt(feedback.getCreatedAt());

        if (feedback.getUserDetails() != null) {
            response.setUserId(feedback.getUserDetails().getUserId());
            response.setUserName(feedback.getUserDetails().getUsername());
            response.setName(feedback.getUserDetails().getName());
            response.setUserEmail(feedback.getUserDetails().getEmail());

        }
        else if (feedback.getClubDetails() != null) {
            response.setUserId(feedback.getClubDetails().getUserId()); // Assuming Club has a similar ID field
            response.setUserName(feedback.getClubDetails().getUsername()); // Map Club Name to Username/Name
            response.setName(feedback.getClubDetails().getName());
            response.setUserEmail(feedback.getClubDetails().getEmail());

        }

        return response;
    }


//    @Override
//    public void setUserOnlineStatus(Long userId, boolean isOnline) {
//        User user = userRepository.findById(userId).orElse(null);
//        if (user != null) {
//            user.setIsOnline(isOnline);
//            user.setLastSeen(LocalDateTime.now());
//            userRepository.save(user);
//        }
//    }

    @Override
    public void setUserOnlineStatus(long parseLong, boolean isOnline) {
        User user = userRepository.findById(parseLong).orElse(null);
        if (user != null) {
            user.setOnline(isOnline);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}