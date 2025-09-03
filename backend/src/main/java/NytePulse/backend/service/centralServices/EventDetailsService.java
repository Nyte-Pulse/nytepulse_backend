package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.EventDetailsDto;
import org.springframework.http.ResponseEntity;

public interface EventDetailsService {

    ResponseEntity<?> saveEvent( EventDetailsDto eventDetailsDto);
}
