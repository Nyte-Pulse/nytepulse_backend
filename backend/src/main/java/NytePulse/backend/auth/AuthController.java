package NytePulse.backend.auth;



import NytePulse.backend.config.JwtTokenProvider;
import NytePulse.backend.dto.ResetPasswordRequest;
import NytePulse.backend.entity.User;
import NytePulse.backend.exception.TokenRefreshException;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.service.EmailService;
import NytePulse.backend.service.centralServices.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private  EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if ("BUSINESS".equals(request.getAccountType())
                    && "PERSONAL".equals(user.getAccountType())) {

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Business Account type is required");
                response.put("status", HttpStatus.BAD_REQUEST.value());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            String jwt = tokenProvider.generateToken(authentication,user.getId(),user.getUsername());

            // Get device info from request headers
            String deviceInfo = httpRequest.getHeader("User-Agent");
            String ipAddress = httpRequest.getRemoteAddr();

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    user.getId(),
                    deviceInfo,
                    ipAddress
            );
            if(refreshToken==null){
                throw new RuntimeException("Could not create refresh token");
            }

            long issuedAtMillis = System.currentTimeMillis();
            JwtResponse jwtResponse = new JwtResponse(jwt, refreshToken.getToken());

            Map<String, Object> response = new HashMap<>();
            response.put("jwtResponse", jwtResponse);
            response.put("issuedAt", issuedAtMillis);
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException ex) {

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid email or password");
            errorResponse.put("status",HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.ok(errorResponse);

        } catch (AuthenticationException ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Authentication failed:"+ex.getMessage());
            errorResponse.put("status",HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        System.out.println("Received refresh token: " + requestRefreshToken);
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = tokenProvider.generateTokenFromEmail(user.getEmail());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // Delete only this specific refresh token (logs out only this device)
        refreshTokenService.deleteByToken(refreshToken);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("Message " , "Logged out successfully from this device");
        return ResponseEntity.ok(response);
    }
    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) throws MessagingException   {
        try {
            if (email == null || email.trim().isEmpty() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return ResponseEntity.badRequest().body("Invalid email address");
            }
            String otp = emailService.sendPasswordResetOtp(email);

            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("Password reset OTP sent to " , email);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("message", "Failed to process request: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            return emailService.resetPassword(request);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("message", "Failed to reset password: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}