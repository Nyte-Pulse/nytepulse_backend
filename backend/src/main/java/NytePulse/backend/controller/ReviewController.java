package NytePulse.backend.controller;

import NytePulse.backend.dto.ReviewAndRatingDto;
import NytePulse.backend.service.centralServices.ReviewAndRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewAndRatingService reviewService;

    @PostMapping("/add/{clubId}")
    public ResponseEntity<?> addReview(  @PathVariable String clubId, @RequestBody ReviewAndRatingDto reviewDto) {
        return reviewService.addReview(clubId,reviewDto);
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<?> getClubReviews(@PathVariable String clubId) {
        return reviewService.getClubReviews(clubId);
    }

}
