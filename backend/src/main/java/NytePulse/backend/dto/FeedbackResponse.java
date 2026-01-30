package NytePulse.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedbackResponse {
    private Long feedbackId;
    private String message;
    private int rating;
    private LocalDateTime createdAt;

    // User Details to show with the feedback
    private String userId;
    private String userName;
    private String userEmail;
    private String name;
}
