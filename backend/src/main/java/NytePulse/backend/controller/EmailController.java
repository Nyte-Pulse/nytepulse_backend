package NytePulse.backend.controller;

import NytePulse.backend.dto.OtpVerificationRequest;
import NytePulse.backend.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;

@RestController
@RequestMapping("/api/auth/email")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/sendOtp")
    public ResponseEntity<String> sendOtp(@RequestParam String to) {
        try {
            if (to == null || to.trim().isEmpty() || !to.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return ResponseEntity.badRequest().body("Invalid email address");
            }
            String otp = String.format("%06d", new SecureRandom().nextInt(999999));
            emailService.sendOtp(to, otp);
            return ResponseEntity.ok("OTP sent successfully to " + to);
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body("Failed to send OTP: " + e.getMessage());
        }
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpVerificationRequest request) {
        try {
            emailService.verifyOtp(request);
            return ResponseEntity.ok("Email verified successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed to verify OTP: " + e.getMessage());
        }
    }
}