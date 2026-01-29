package NytePulse.backend.dto;

public class MusicTrackDto {
    private Long id;
    private String title;
    private String audioUrl;
    private String coverImageUrl;

    // Constructors, Getters, Setters
    public MusicTrackDto(Long id, String title, String audioUrl, String coverImageUrl) {
        this.id = id;
        this.title = title;
        this.audioUrl = audioUrl;
        this.coverImageUrl = coverImageUrl;
    }

    // Standard getters/setters...
}