package NytePulse.backend.service.centralServices;

import NytePulse.backend.auth.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface UserService {

    ResponseEntity<?> register(RegisterRequest request);
}
