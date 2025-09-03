package NytePulse.backend.service.impl;

import NytePulse.backend.auth.RegisterRequest;
import NytePulse.backend.entity.ClubDetails;
import NytePulse.backend.entity.Role;
import NytePulse.backend.entity.User;
import NytePulse.backend.entity.UserDetails;
import NytePulse.backend.exception.AppException;
import NytePulse.backend.repository.ClubDetailsRepository;
import NytePulse.backend.repository.RoleRepository;
import NytePulse.backend.repository.UserDetailsRepository;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.service.centralServices.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClubDetailsRepository clubDetailsRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final ZoneId SRI_LANKA_ZONE = ZoneId.of("Asia/Colombo");

    private String generateUserId(String accountType) {
        String prefix = "US";
        if ("BUSINESS".equalsIgnoreCase(accountType)) {
            prefix = "BS";
        }

        // Fetch last user by accountType ordered by userId descending
        User lastUser = userRepository.findTopByAccountTypeOrderByUserIdDesc(accountType);
        String lastUserId = (lastUser != null) ? lastUser.getUserId() : null;

        if (lastUserId == null) {
            return prefix + "00001";
        }

        try {
            String numberPart = lastUserId.substring(2); // Extract number part (assumes format XX00000)
            int number = Integer.parseInt(numberPart);
            number++;
            return String.format(prefix + "%05d", number);
        } catch (Exception e) {
            // Fallback if parsing fails
            logger.warn("Failed to parse userId: {}, using default", lastUserId);
            return prefix + "00001";
        }
    }

    @Override
    public ResponseEntity<?> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Email is already taken!");
        }

        User user = new User(request.getUsername(), request.getEmail(), passwordEncoder.encode(request.getPassword()));
        user.setAccountType(request.getAccountType());

        String userId = generateUserId(request.getAccountType());
        user.setUserId(userId);
        LocalDateTime sriLankanTime = LocalDateTime.now(SRI_LANKA_ZONE);
        user.setCreatedAt(sriLankanTime);

        Set<Role> roles = new HashSet<>();

        Role.ERole roleEnum;
        try {
            roleEnum = Role.ERole.valueOf("ROLE_" + request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid role: " + request.getRole());
        }

        Role userRole = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);

        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        logger.info("User details saved successfully: {}", savedUser);

        UserDetails userDetails = new UserDetails(
                savedUser.getUserId(),  // Pass userId as String
                request.getEmail(),
                request.getUsername(),
                request.getName() != null ? request.getName() : request.getUsername(),
                request.getAccountType()
        );

        if("USER".equals(request.getAccountType())){
            UserDetails savedUserDetails = userDetailsRepository.save(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "user details saved successfully");
            response.put("userId", savedUser.getUserId());
            response.put("username", savedUserDetails.getUsername());
            response.put("email", savedUserDetails.getEmail());
            response.put("updated_at", LocalDateTime.now(SRI_LANKA_ZONE));
            logger.info("User and UserDetails saved successfully: {}", savedUserDetails);


        } else if ("BUSINESS".equals(request.getAccountType())) {

            ClubDetails clubDetails = new ClubDetails(
                    savedUser.getUserId(),
                    request.getEmail(),
                    request.getUsername(),
                    request.getName() != null ? request.getName() : request.getUsername(),
                    request.getAccountType()
            );

            ClubDetails savedClubDetails = clubDetailsRepository.save(clubDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Club details saved successfully");
            response.put("bio", savedUser.getUserId());
            response.put("username", savedClubDetails.getUsername());
            response.put("email", savedClubDetails.getEmail());
            response.put("updated_at", LocalDateTime.now(SRI_LANKA_ZONE));
            logger.info("ClubDetails saved successfully: {}", savedClubDetails);
        }

        return ResponseEntity.ok(savedUser);
    }
}
