package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.ProfileLinkResponse;
import org.springframework.http.ResponseEntity;

public interface ProfileLinkService {
    ResponseEntity<?> generateProfileLink(String userId);

    ResponseEntity<?> generateProfileLinkByUsername(String username);

    ResponseEntity<?> getUserProfileByUsername(String username);

    ResponseEntity<?> getClubProfileByUsername(String username);
}
