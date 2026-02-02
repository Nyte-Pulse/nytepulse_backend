package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.UserDetailsDto;
import NytePulse.backend.dto.UserDetailsUpdateRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface UserDetailsService {
    ResponseEntity<?> updateUserDetails(String userId, UserDetailsDto userDetailsDto);

    ResponseEntity<?> setAccountPrivateOrPublic(String userId, Boolean isPrivate);


    ResponseEntity<?> getAccountNameByEmail(String email);

    ResponseEntity<?> searchAccountByName(String name, Pageable pageable);

    ResponseEntity<?> getAllBusinessAccount();

    ResponseEntity<?> searchFollowerAccountByName(Long userId, String trim, Pageable pageable);

    ResponseEntity<?> getMentionedAllowUserList(Long currentUserId);

    ResponseEntity<?> getTaggedAllowUserList(Long currentUserId);

    ResponseEntity<?> checkEmailAvailability(String email);
}
