package NytePulse.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerDetailDto {

    private Long userId;

    private String organizerName;
    private String organizerContact;
    private String organizerEmail;
    private String websiteUrl;
}
