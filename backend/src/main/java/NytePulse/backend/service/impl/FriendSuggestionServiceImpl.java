package NytePulse.backend.service.impl;

import NytePulse.backend.dto.UserSuggestionDTO;
import NytePulse.backend.entity.User;
import NytePulse.backend.repository.UserRelationshipRepository;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.service.centralServices.FriendSuggestionService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FriendSuggestionServiceImpl implements FriendSuggestionService {

    @Autowired
    private UserRelationshipRepository userRelationshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseEntity<?> getFriendSuggestions(String currentUserId, int limit) {
        try {
            log.info("Fetching friend suggestions for user: {}", currentUserId);

            Optional<User> currentUser = userRepository.findByEmail(currentUserId);

            if (!currentUser.isPresent()) {
                log.warn("User not found with email: {}", currentUserId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found", "suggestions", new ArrayList<>()));
            }

            List<UserSuggestionDTO> mutualFollowerSuggestions =
                    getMutualFollowerSuggestions(currentUser.get().getId(), limit);

            List<UserSuggestionDTO> followingNetworkSuggestions =
                    getFollowingNetworkSuggestions(currentUser.get().getId(), limit);

            // Merge and deduplicate by userId
            Map<String, UserSuggestionDTO> suggestionMap = new LinkedHashMap<>();

            for (UserSuggestionDTO suggestion : mutualFollowerSuggestions) {
                if (!suggestionMap.containsKey(suggestion.getUserId())) {
                    suggestionMap.put(suggestion.getUserId(), suggestion);
                } else {
                    // If exists, keep the one with higher mutual friends count
                    UserSuggestionDTO existing = suggestionMap.get(suggestion.getUserId());
                    if (suggestion.getMutualFriendsCount() > existing.getMutualFriendsCount()) {
                        suggestionMap.put(suggestion.getUserId(), suggestion);
                    }
                }
            }

            for (UserSuggestionDTO suggestion : followingNetworkSuggestions) {
                if (!suggestionMap.containsKey(suggestion.getUserId())) {
                    suggestionMap.put(suggestion.getUserId(), suggestion);
                }
            }

            List<UserSuggestionDTO> finalSuggestions = suggestionMap.values().stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            log.info("Successfully fetched {} friend suggestions for user: {}",
                    finalSuggestions.size(), currentUserId);


            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Successfully fetched friend suggestions");
            response.put("result", finalSuggestions);
            return ResponseEntity.ok(response);

        } catch (NoSuchElementException e) {
            log.error("User not found error for userId: {}", currentUserId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found", "message", e.getMessage(),"status", HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            log.error("Error fetching friend suggestions for user: {}", currentUserId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch friend suggestions",
                            "message", e.getMessage(),"status", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    private List<UserSuggestionDTO> getMutualFollowerSuggestions(Long currentUserId, int limit) {
        try {
            log.debug("Fetching mutual follower suggestions for userId: {}", currentUserId);
            List<Object[]> results = userRelationshipRepository
                    .findFriendSuggestionsByMutualFollowers(currentUserId, limit);
            return convertToDTO(results);
        } catch (Exception e) {
            log.error("Error fetching mutual follower suggestions for userId: {}",
                    currentUserId, e);
            return new ArrayList<>();
        }
    }

    private List<UserSuggestionDTO> getFollowingNetworkSuggestions(Long currentUserId, int limit) {
        try {
            log.debug("Fetching following network suggestions for userId: {}", currentUserId);
            List<Object[]> results = userRelationshipRepository
                    .findFriendSuggestionsFromFollowingNetwork(currentUserId, limit);
            return convertToDTO(results);
        } catch (Exception e) {
            log.error("Error fetching following network suggestions for userId: {}",
                    currentUserId, e);
            return new ArrayList<>();
        }
    }

    private List<UserSuggestionDTO> convertToDTO(List<Object[]> results) {
        Map<String, UserSuggestionDTO> uniqueUsers = new LinkedHashMap<>();

        try {
            for (Object[] row : results) {
                try {
                    if (row == null || row.length < 6) {
                        log.warn("Invalid row data encountered, skipping...");
                        continue;
                    }

                    String userId = (String) row[0];

                    // Skip if already processed
                    if (uniqueUsers.containsKey(userId)) {
                        continue;
                    }

                    UserSuggestionDTO dto = new UserSuggestionDTO(
                            userId,                                    // userId
                            (String) row[1],                          // username
                            (String) row[2],                          // name
                            (String) row[3],                          // profilePicture
                            row[4] != null ? ((Number) row[4]).longValue() : 0L,  // mutualFriendsCount
                            (String) row[5]                           // accountType
                    );
                    uniqueUsers.put(userId, dto);

                } catch (ClassCastException | NullPointerException e) {
                    log.error("Error converting row to DTO, skipping row", e);
                    continue;
                }
            }
        } catch (Exception e) {
            log.error("Error during DTO conversion", e);
        }

        return new ArrayList<>(uniqueUsers.values());
    }

    @Override
    public ResponseEntity<?> getMutualFriends(String userId1, String userId2) {
        try {
            log.info("Fetching mutual friends between {} and {}", userId1, userId2);

            Optional<User> user1Opt = userRepository.findByEmail(userId1);
            User user2 = userRepository.findByUserId(userId2);

            if (!user1Opt.isPresent()) {
                log.warn("User1 not found with email: {}", userId1);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User1 not found",
                                "mutualFriends", new ArrayList<>()));
            }

            if (user2 == null) {
                log.warn("User2 not found with userId: {}", userId2);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User2 not found",
                                "mutualFriends", new ArrayList<>()));
            }

            User user1 = user1Opt.get();
            log.debug("User1 ID: {}, User2 ID: {}", user1.getId(), user2.getId());

            List<Object[]> results = userRelationshipRepository.findMutualFriends(
                    user1.getId(),
                    user2.getId()
            );

            List<UserSuggestionDTO> mutualFriends = convertToDTO(results);

            log.info("Found {} mutual friends between {} and {}",
                    mutualFriends.size(), userId1, userId2);

            return ResponseEntity.ok(mutualFriends);

        } catch (NoSuchElementException e) {
            log.error("User not found error", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "One or both users not found",
                            "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching mutual friends between {} and {}",
                    userId1, userId2, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch mutual friends",
                            "message", e.getMessage()));
        }
    }
}
