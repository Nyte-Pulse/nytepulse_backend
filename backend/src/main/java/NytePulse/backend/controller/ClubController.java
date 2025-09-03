package NytePulse.backend.controller;


import NytePulse.backend.dto.ClubDetailsDto;
import NytePulse.backend.service.centralServices.ClubDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/club-details")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClubController {

    @Autowired
    private ClubDetailsService clubDetailsService;

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateClubDetails(
            @PathVariable String userId,
            @RequestBody ClubDetailsDto clubDetailsDto) {
        return clubDetailsService.updateClubDetails(userId, clubDetailsDto);
    }

}
