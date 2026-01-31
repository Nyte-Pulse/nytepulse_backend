package NytePulse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadReceiptDTO {
    private Long conversationId;
    private Long readerId; // The user who read the messages
    private LocalDateTime readAt;
}