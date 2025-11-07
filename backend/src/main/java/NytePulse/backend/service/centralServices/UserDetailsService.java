package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.UserDetailsDto;
import NytePulse.backend.dto.UserDetailsUpdateRequest;
import org.springframework.http.ResponseEntity;

public interface UserDetailsService {
    ResponseEntity<?> updateUserDetails(String userId, UserDetailsDto userDetailsDto);

    ResponseEntity<?> setAccountPrivateOrPublic(String userId, Boolean isPrivate);


}
