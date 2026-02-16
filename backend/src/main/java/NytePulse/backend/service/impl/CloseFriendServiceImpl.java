package NytePulse.backend.service.impl;

import NytePulse.backend.entity.CloseFriend;
import NytePulse.backend.entity.UserDetails;
import NytePulse.backend.repository.CloseFriendRepository;
import NytePulse.backend.repository.UserDetailsRepository;
import NytePulse.backend.repository.UserRelationshipRepository;
import NytePulse.backend.service.centralServices.CloseFriendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CloseFriendServiceImpl implements CloseFriendService {

    private static final Logger log = LoggerFactory.getLogger(CloseFriendServiceImpl.class);

    @Autowired
    private CloseFriendRepository closeFriendRepository;

    @Autowired
    private UserRelationshipRepository userRelationshipRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    // Add user to close friends
    @Transactional
    public ResponseEntity<?> addCloseFriend(String userId, String closeFriendUserId) {
        try {

            if (userId.equals(closeFriendUserId)) {
                return ResponseEntity.badRequest().body("Cannot add yourself to close friends");
            }

            Optional<CloseFriend> existing = closeFriendRepository
                    .findByUserIdAndCloseFriendUserId(userId, closeFriendUserId);

            if (existing.isPresent()) {
                return ResponseEntity.badRequest().body("User already in close friends");
            }

            // Check if they follow each other (optional - Instagram allows only followers)
            boolean isFollower = userRelationshipRepository.existsByFollower_UserIdAndFollowing_UserId(closeFriendUserId, userId);


            if (!isFollower) {
                return ResponseEntity.badRequest().body("User must be your follower to add to close friends");
            }

            CloseFriend closeFriend = new CloseFriend();
            closeFriend.setUserId(userId);
            closeFriend.setCloseFriendUserId(closeFriendUserId);
            closeFriendRepository.save(closeFriend);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Added to close friends successfully");
            response.put("userId", userId);
            response.put("closeFriendUserId", closeFriendUserId);
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error adding close friend: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add close friend");
        }
    }

    @Transactional
    public ResponseEntity<?> removeCloseFriend(String userId, String closeFriendUserId) {
        try {
            closeFriendRepository.deleteByUserIdAndCloseFriendUserId(userId, closeFriendUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Removed from close friends successfully");
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error removing close friend: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to remove close friend");
        }
    }

    
    public ResponseEntity<?> getCloseFriends(String userId) {
        try {
            List<CloseFriend> closeFriends = closeFriendRepository.findByUserId(userId);

            List<Map<String, Object>> friendsList = closeFriends.stream()
                    .map(cf -> {
                        UserDetails user = userDetailsRepository.findByUserId(cf.getCloseFriendUserId());

                        if (user == null) {
                            return null;
                        }

                        Map<String, Object> friendData = new HashMap<>();
                        friendData.put("userId", user.getUserId());
                        friendData.put("username", user.getUsername());
                        friendData.put("name", user.getName());
                        friendData.put("profilePicture", user.getProfilePicture());
                        friendData.put("addedAt", cf.getAddedAt());
                        return friendData;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("totalCloseFriends", friendsList.size());
            response.put("closeFriends", friendsList);
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching close friends: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch close friends");
        }
    }
    public boolean isCloseFriend(String userId, String viewerUserId) {
        return closeFriendRepository.existsByUserIdAndCloseFriendUserId(userId, viewerUserId);
    }
}
