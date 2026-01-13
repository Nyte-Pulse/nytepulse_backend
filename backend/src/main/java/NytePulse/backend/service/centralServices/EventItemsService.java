package NytePulse.backend.service.centralServices;

import org.springframework.http.ResponseEntity;

public interface EventItemsService {
    ResponseEntity<?> initializeData();
    ResponseEntity<?> getItemsByCategory(String category);
}
