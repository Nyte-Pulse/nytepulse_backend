package NytePulse.backend.service.centralServices;

import NytePulse.backend.auth.RegisterRequest;
import NytePulse.backend.dto.UserDetailsUpdateRequest;
import org.springframework.http.ResponseEntity;

public interface UserService {

    ResponseEntity<?> register(RegisterRequest request);



}
