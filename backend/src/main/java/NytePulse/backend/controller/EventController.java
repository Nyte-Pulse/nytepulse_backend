package NytePulse.backend.controller;


import NytePulse.backend.dto.EventDetailsDto;
import NytePulse.backend.service.centralServices.EventDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EventController {

    @Autowired
    private EventDetailsService eventDetailsService;

    @PostMapping("/save")
    public ResponseEntity<?> saveEvent(@RequestBody EventDetailsDto eventDetailsDto){
        return ResponseEntity.ok(eventDetailsService.saveEvent(eventDetailsDto));
    }
}
