package NytePulse.backend.dto;

import lombok.Data;

@Data
public class FeedbackRequest {
    private String userId;
    private String message;
    private int rating;
}