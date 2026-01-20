package NytePulse.backend.dto;

import lombok.Data;

@Data
public class StartStreamRequest {
    private Long userId; // The ID of the user going live
    private String title; // "Friday Night Chat!"
}