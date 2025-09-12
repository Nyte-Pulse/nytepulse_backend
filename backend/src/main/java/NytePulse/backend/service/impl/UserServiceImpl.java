package NytePulse.backend.service.impl;

import NytePulse.backend.auth.RegisterRequest;
import NytePulse.backend.entity.*;
import NytePulse.backend.repository.*;
import NytePulse.backend.service.centralServices.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


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
    private ClubDetailsRepository clubDetailsRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final ZoneId SRI_LANKA_ZONE = ZoneId.of("Asia/Colombo");

    private String generateUserId(String accountType) {
        String prefix = "US";
        if ("BUSINESS".equalsIgnoreCase(accountType)) {
            prefix = "BS";
        }

        // Fetch last user by accountType ordered by userId descending
        User lastUser = userRepository.findTopByAccountTypeOrderByUserIdDesc(accountType);
        String lastUserId = (lastUser != null) ? lastUser.getUserId() : null;

        if (lastUserId == null) {
            return prefix + "00001";
        }

        try {
            String numberPart = lastUserId.substring(2); // Extract number part (assumes format XX00000)
            int number = Integer.parseInt(numberPart);
            number++;
            return String.format(prefix + "%05d", number);
        } catch (Exception e) {
            // Fallback if parsing fails
            logger.warn("Failed to parse userId: {}, using default", lastUserId);
            return prefix + "00001";
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
                savedUser.getUserId(),  // Pass userId as String
                request.getEmail(),
                request.getUsername(),
                request.getName() != null ? request.getName() : request.getUsername(),
                request.getAccountType()
        );

        if ("USER".equals(request.getAccountType())) {
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
                    request.getName() != null ? request.getName() : request.getUsername(),
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

        return ResponseEntity.ok(savedUser);
    }

    @Override
    public ResponseEntity<?> followUser(String followerUserId, String followingUserId) {
        try {
            if (followerUserId.equals(followingUserId)) {
                throw new IllegalArgumentException("User cannot follow themselves");
            }

            // Check if already following
            if (relationshipRepository.isFollowing(followerUserId, followingUserId)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Already following user: " + followingUserId);

            }
            // Find users
            User follower = userRepository.findByUserId(followerUserId);

            User following = userRepository.findByUserId(followingUserId);

            // Create relationship
            UserRelationship relationship = new UserRelationship(follower, following);
            relationshipRepository.save(relationship);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully followed user: " + followingUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error while following user: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while trying to follow the user.");
        }

    }


    @Override
    public ResponseEntity<?> getFollowers(String userId) {
        try {
            Page<User> followers = relationshipRepository.getFollowers(userId, Pageable.unpaged());

            List<Map<String, String>> followerList = new ArrayList<>();
            for (User follower : followers) {
                Map<String, String> followerInfo = new HashMap<>();
                followerInfo.put("userId", follower.getUserId());
                followerInfo.put("username", follower.getUsername());
                followerInfo.put("email", follower.getEmail());
                followerList.add(followerInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("count", followerList.size());
            response.put("followers", followerList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error while fetching followers: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while trying to fetch followers.");
        }
    }

    @Override
    public ResponseEntity<?> getFollowing(String userId) {
        try {
            Page<User> following = relationshipRepository.getFollowing(userId, Pageable.unpaged());

            List<Map<String, String>> followingList = new ArrayList<>();
            for (User followee : following) {
                Map<String, String> followeeInfo = new HashMap<>();
                followeeInfo.put("userId", followee.getUserId());
                followeeInfo.put("username", followee.getUsername());
                followeeInfo.put("email", followee.getEmail());
                followingList.add(followeeInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("count", followingList.size());
            response.put("followers", followingList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error while fetching following: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while trying to fetch following.");
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
    public Boolean isFollowing(String followerUserId, String followingUserId) {
        return relationshipRepository.isFollowing(followerUserId, followingUserId);
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

}
