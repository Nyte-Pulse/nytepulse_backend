package NytePulse.backend.dto;

import NytePulse.backend.enums.StreamVisibility;
import lombok.Data;

@Data
public class StartStreamRequestDTO {
    // User chooses this in the app before going live
    private StreamVisibility visibility;
}