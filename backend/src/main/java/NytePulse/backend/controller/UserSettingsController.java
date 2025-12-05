package NytePulse.backend.controller;

import NytePulse.backend.dto.UpdateSettingsRequest;
import NytePulse.backend.dto.UserSettingsDTO;
import NytePulse.backend.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsService settingsService;

    @PostMapping("/create-default/{userId}")
    public ResponseEntity<?> createDefaultSettings(@PathVariable Long userId) {
        try {
            UserSettingsDTO settings = settingsService.createDefaultSettings(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Default settings created",
                    "settings", settings
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getSettings(@PathVariable Long userId) {
        System.out.println("Fetching settings for userId: " + userId);
        UserSettingsDTO settings = settingsService.getSettings(userId);
        return ResponseEntity.ok(Map.of("settings", settings));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateSettings(
            @PathVariable Long userId,
            @RequestBody UpdateSettingsRequest request) {
        UserSettingsDTO updated = settingsService.updateSettings(userId, request);
        return ResponseEntity.ok(Map.of("settings", updated));
    }
}
