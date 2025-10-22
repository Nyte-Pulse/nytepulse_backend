//package NytePulse.backend.service;
//
//import NytePulse.backend.dto.OtpVerificationRequest;
//import NytePulse.backend.dto.ResetPasswordRequest;
//import NytePulse.backend.entity.Otp;
//import NytePulse.backend.entity.User;
//import NytePulse.backend.repository.OtpRepository;
//import NytePulse.backend.repository.UserRepository;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.security.SecureRandom;
//import java.time.LocalDateTime;
//
//@Service
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//    private final OtpRepository otpRepository;
//
//    private final UserRepository userRepository;
//
//    private final PasswordEncoder passwordEncoder;
//
//    @Value("${sendgrid.api.key}")
//    private String sendGridApiKey;
//
//    @Autowired
//    public EmailService(JavaMailSender mailSender, OtpRepository otpRepository,UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        this.mailSender = mailSender;
//        this.otpRepository = otpRepository;
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//
//    }
//
//    @Transactional
//    public String sendPasswordResetOtp(String email) throws MessagingException {
//        if (!userRepository.findByEmail(email).isPresent()) {
//            throw new RuntimeException("User not found");
//        }
//        String otp = String.format("%06d", new SecureRandom().nextInt(999999));
//        MimeMessage mime = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "utf-8");
//
//        String html = """
//            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
//                <h3>Your OTP for Password Reset</h3>
//                <p>We received a request to reset your password. Please use the following One-Time Password (OTP) to proceed:</p>
//                <p style="font-size: 24px; font-weight: bold; color: #2c3e50; background-color: #f1f1f1; padding: 10px; text-align: center;">%s</p>
//                <p>This code will expire in 5 minutes for security reasons.</p>
//                <p>If you did not request a password reset, please ignore this email or contact our support team at <a href="mailto:support@yourdomain.com">support@yourdomain.com</a>.</p>
//                <p>Best regards,<br>NytePulse Team</p>
//                <p style="font-size: 12px; color: #888;">This is an automated message. Please do not reply directly to this email.</p>
//            </div>
//            """.formatted(otp);
//
//        helper.setTo(email);
//        helper.setSubject("Your OTP Code for Password Reset");
//        helper.setText(html, true);
//        helper.setFrom("otp@nytepulse.com");
//        helper.setReplyTo("support@nytepulse.com");
//
//        mailSender.send(mime);
//
//        Otp otpEntity = new Otp(email, otp, LocalDateTime.now().plusMinutes(5));
//        otpRepository.save(otpEntity);
//        return otp; // For debugging; remove in production
//    }
//
//    @Transactional
//    public ResponseEntity<?> sendOtp(String to, String otp) throws MessagingException {
//        MimeMessage mime = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "utf-8");
//
//        String html = """
//            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
//                <h3>Your OTP for Email Verification</h3>
//                <p>Thank you for using our service. Please use the following One-Time Password (OTP) to verify your email address:</p>
//                <p style="font-size: 24px; font-weight: bold; color: #2c3e50; background-color: #f1f1f1; padding: 10px; text-align: center;">%s</p>
//                <p>This code will expire in 5 minutes for security reasons.</p>
//                <p>If you did not request this OTP, please ignore this email or contact our support team at <a href="mailto:support@nytepulse.com">support@nytepulse.com</a>.</p>
//                <p>Best regards,<br>Nyte Pulse App Team</p>
//                <p style="font-size: 12px; color: #888;">This is an automated message. Please do not reply directly to this email.</p>
//            </div>
//            """.formatted(otp);
//
//        helper.setTo(to);
//        helper.setSubject("Your OTP Code for Verification");
//        helper.setText(html, true);
//        helper.setFrom("otp@nytepulse.com");
//        helper.setReplyTo("support@nytepulse.com");
//
//        mailSender.send(mime);
//        Otp otpEntity = new Otp(to, otp, LocalDateTime.now().plusMinutes(5));
//        otpRepository.save(otpEntity);
//        return ResponseEntity.ok("OTP sent successfully to " + to);
//    }
//
//    @Transactional
//    public ResponseEntity<?> verifyOtp(OtpVerificationRequest request) throws RuntimeException {
//        if (request.getEmail() == null || request.getOtp() == null ||
//                !request.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
//            return ResponseEntity
//                    .status(HttpStatus.BAD_REQUEST)
//                    .body("Invalid email or OTP");
//        }
//
//        Otp otp = otpRepository.findById(request.getEmail())
//                .orElseThrow(() -> new RuntimeException("No OTP found for email"));
//
//        if (otp.getExpiry().isBefore(LocalDateTime.now())) {
//            otpRepository.delete(otp);
//            return ResponseEntity
//                    .status(HttpStatus.BAD_REQUEST)
//                    .body("OTP has expired");
//        }
//
//        if (!otp.getOtp().equals(request.getOtp())) {
//            return ResponseEntity
//                    .status(HttpStatus.BAD_REQUEST)
//                    .body("Invalid OTP");
//        }
//
//        otpRepository.delete(otp); // Delete OTP after successful verification
//        return ResponseEntity.ok("Email verified successfully");
//    }
//
//    @Transactional
//    public ResponseEntity<?> resetPassword(ResetPasswordRequest request) throws RuntimeException {
//        if (request.getEmail() == null || !request.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
//            throw new RuntimeException("Invalid email");
//        }
//        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
//            return ResponseEntity
//                    .status(HttpStatus.BAD_REQUEST)
//                    .body("Password must be at least 8 characters");
//
//        }
//
//        User user = userRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
//        userRepository.save(user);
//        return ResponseEntity.ok("Password Reset successfully");
//    }
//}

package NytePulse.backend.service;

import NytePulse.backend.dto.OtpVerificationRequest;
import NytePulse.backend.dto.ResetPasswordRequest;
import NytePulse.backend.entity.Otp;
import NytePulse.backend.entity.User;
import NytePulse.backend.repository.OtpRepository;
import NytePulse.backend.repository.UserRepository;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class EmailService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Autowired
    public EmailService(OtpRepository otpRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String sendPasswordResetOtp(String email) throws IOException {
        if (!userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User not found");
        }

        String otp = String.format("%06d", new SecureRandom().nextInt(999999));

        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h3>Your OTP for Password Reset</h3>
                <p>We received a request to reset your password. Please use the following One-Time Password (OTP) to proceed:</p>
                <p style="font-size: 24px; font-weight: bold; color: #2c3e50; background-color: #f1f1f1; padding: 10px; text-align: center;">%s</p>
                <p>This code will expire in 5 minutes for security reasons.</p>
                <p>If you did not request a password reset, please ignore this email or contact our support team at <a href="mailto:support@nytepulse.com">support@nytepulse.com</a>.</p>
                <p>Best regards,<br>NytePulse Team</p>
                <p style="font-size: 12px; color: #888;">This is an automated message. Please do not reply directly to this email.</p>
            </div>
            """.formatted(otp);

        sendEmail(email, "Your OTP Code for Password Reset", html);

        Otp otpEntity = new Otp(email, otp, LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otpEntity);
        return otp; // For debugging; remove in production
    }

    @Transactional
    public ResponseEntity<?> sendOtp(String to, String otp) throws IOException {
        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h3>Your OTP for Email Verification</h3>
                <p>Thank you for using our service. Please use the following One-Time Password (OTP) to verify your email address:</p>
                <p style="font-size: 24px; font-weight: bold; color: #2c3e50; background-color: #f1f1f1; padding: 10px; text-align: center;">%s</p>
                <p>This code will expire in 5 minutes for security reasons.</p>
                <p>If you did not request this OTP, please ignore this email or contact our support team at <a href="mailto:support@nytepulse.com">support@nytepulse.com</a>.</p>
                <p>Best regards,<br>Nyte Pulse App Team</p>
                <p style="font-size: 12px; color: #888;">This is an automated message. Please do not reply directly to this email.</p>
            </div>
            """.formatted(otp);

        sendEmail(to, "Your OTP Code for Verification", html);

        Otp otpEntity = new Otp(to, otp, LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otpEntity);
        return ResponseEntity.ok("OTP sent successfully to " + to);
    }

    private void sendEmail(String to, String subject, String htmlContent) throws IOException {
        Email from = new Email("otp@nytepulse.com", "NytePulse");
        Email toEmail = new Email(to);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, toEmail, content);

        // Set reply-to
        mail.setReplyTo(new Email("support@nytepulse.com"));

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            // Log response for debugging
            System.out.println("SendGrid Status Code: " + response.getStatusCode());
            System.out.println("SendGrid Response Body: " + response.getBody());

            if (response.getStatusCode() >= 400) {
                throw new IOException("Failed to send email: " + response.getBody());
            }
        } catch (IOException ex) {
            throw new IOException("Error sending email via SendGrid: " + ex.getMessage(), ex);
        }
    }

    @Transactional
    public ResponseEntity<?> verifyOtp(OtpVerificationRequest request) throws RuntimeException {
        if (request.getEmail() == null || request.getOtp() == null ||
                !request.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid email or OTP");
        }

        Otp otp = otpRepository.findById(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No OTP found for email"));

        if (otp.getExpiry().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otp);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("OTP has expired");
        }

        if (!otp.getOtp().equals(request.getOtp())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid OTP");
        }

        otpRepository.delete(otp);
        return ResponseEntity.ok("Email verified successfully");
    }

    @Transactional
    public ResponseEntity<?> resetPassword(ResetPasswordRequest request) throws RuntimeException {
        if (request.getEmail() == null || !request.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new RuntimeException("Invalid email");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Password must be at least 8 characters");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Password Reset successfully");
    }
}
