package NytePulse.backend.controller;

import NytePulse.backend.dto.OtpVerificationRequest;
import NytePulse.backend.dto.ResetPasswordRequest;
import NytePulse.backend.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

@RestController
@RequestMapping("/api/otp/email")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/sendOtp")
    public ResponseEntity<?> sendOtp(@RequestParam String to) {
        try {
            if (to == null || to.trim().isEmpty() || !to.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return ResponseEntity.badRequest().body("Invalid email address");
            }
            String otp = String.format("%06d", new SecureRandom().nextInt(999999));
            return emailService.sendOtp(to, otp);
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body("Failed to send OTP: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {
        try {
            return emailService.verifyOtp(request);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed to verify OTP: " + e.getMessage());
        }
    }

//    @PostMapping("/request-password-reset")
//    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) throws UnsupportedEncodingException {
//        try {
//            if (email == null || email.trim().isEmpty() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
//                return ResponseEntity.badRequest().body("Invalid email address");
//            }
//            String otp = emailService.sendPasswordResetOtp(email);
//            System.out.println("Password reset OTP sent to: " + email + ", OTP: " + otp);
//            return ResponseEntity.ok("Password reset OTP sent to " + email);
//        } catch (RuntimeException | MessagingException e) {
//            return ResponseEntity.badRequest().body("Failed to process request: " + e.getMessage());
//        }
//    }
}
