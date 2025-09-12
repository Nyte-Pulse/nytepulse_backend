package NytePulse.backend.controller;


import NytePulse.backend.dto.ClubDetailsDto;
import NytePulse.backend.service.centralServices.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/club-details")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClubController {

    @Autowired
    private ClubService clubService;

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateClubDetails(
            @PathVariable String userId,
            @RequestBody ClubDetailsDto clubDetailsDto) {
        return clubService.updateClubDetails(userId, clubDetailsDto);
    }

    @GetMapping("/getClubById/{userId}")
    public ResponseEntity<?> getClubDetailsByUserId(@PathVariable String userId) {
        return clubService.getClubDetailsByUserId(userId);
    }

}
