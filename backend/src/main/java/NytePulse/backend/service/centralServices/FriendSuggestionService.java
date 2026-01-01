package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.UserSuggestionDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface FriendSuggestionService {
    ResponseEntity<?> getFriendSuggestions(String currentUserId, int limit);

    ResponseEntity<?> getMutualFriends(String currentUserId, String targetUserId);
}
