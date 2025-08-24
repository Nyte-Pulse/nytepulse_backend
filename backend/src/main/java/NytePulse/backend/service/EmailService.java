package NytePulse.backend.service;

import NytePulse.backend.dto.OtpVerificationRequest;
import NytePulse.backend.entity.Otp;
import NytePulse.backend.repository.OtpRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final OtpRepository otpRepository;

    @Autowired
    public EmailService(JavaMailSender mailSender, OtpRepository otpRepository) {
        this.mailSender = mailSender;
        this.otpRepository = otpRepository;
    }

    public void sendOtp(String to, String otp) throws MessagingException {
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "utf-8");

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

        helper.setTo(to);
        helper.setSubject("Your OTP Code for Verification");
        helper.setText(html, true);
        helper.setFrom("contact@ontocriptit.com");
        helper.setReplyTo("support@yourdomain.com");

        mailSender.send(mime);
        Otp otpEntity = new Otp(to, otp, LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otpEntity);
    }

    public void verifyOtp(OtpVerificationRequest request) throws RuntimeException {
        if (request.getEmail() == null || request.getOtp() == null ||
                !request.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new RuntimeException("Invalid email or OTP");
        }

        Otp otp = otpRepository.findById(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No OTP found for email"));

        if (otp.getExpiry().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otp);
            throw new RuntimeException("OTP has expired");
        }

        if (!otp.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        otpRepository.delete(otp); // Delete OTP after successful verification
    }
}