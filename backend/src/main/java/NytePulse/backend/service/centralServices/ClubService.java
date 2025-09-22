package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.ClubDetailsDto;
import org.springframework.http.ResponseEntity;

public interface ClubService {

    ResponseEntity<?> updateClubDetails(String userId, ClubDetailsDto clubDetailsDto);

    ResponseEntity<?> getClubDetailsByUserId(String userId);
}
