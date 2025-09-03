package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.ClubDetailsDto;
import NytePulse.backend.dto.UserDetailsUpdateRequest;
import org.springframework.http.ResponseEntity;

public interface ClubDetailsService {

    ResponseEntity<?> updateClubDetails(String userId, ClubDetailsDto clubDetailsDto);

}
