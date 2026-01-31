package NytePulse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SocketEvent {
    private String type; // e.g., "MESSAGE", "MESSAGE_READ", "TYPING"
    private Object data; // The actual payload (MessageDTO or ReadReceiptDTO)
}