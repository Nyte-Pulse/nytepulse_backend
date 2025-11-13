package NytePulse.backend.controller;


import NytePulse.backend.dto.EventDetailsDto;
import NytePulse.backend.dto.SaveEventDto;
import NytePulse.backend.dto.ReportEventDto;
import NytePulse.backend.service.centralServices.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/event")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping("/save")
    public ResponseEntity<?> saveEvent(@RequestBody EventDetailsDto eventDetailsDto){
        return ResponseEntity.ok(eventService.saveEvent(eventDetailsDto));
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchEvents(@RequestBody EventDetailsDto eventDetailsDto) {
        return eventService.searchEvents(eventDetailsDto);
    }

    @GetMapping("/getEventById/{eventId}")
    public ResponseEntity<?> getEventById(@PathVariable String eventId) {
        return eventService.getEventById(eventId);
    }

    @PostMapping("/saveEventByUser")
    public ResponseEntity<?> saveEventByUser(@RequestBody SaveEventDto saveEventDto) {
        return eventService.saveEventByUser(saveEventDto);
    }

    @GetMapping("/getSavedEventsByUser/{userId}")
    public ResponseEntity<?> getSavedEventsByUser(@PathVariable String userId) {
        return eventService.getSavedEventsByUser(userId);
    }

    @PostMapping("/eventReport")
    public ResponseEntity<?> reportEvent(@RequestBody ReportEventDto reportEventDto){
        return eventService.reportEvent(reportEventDto);
    }

    @GetMapping("/getAllReportedEvents")
    public ResponseEntity<?> getAllReportedEvents() {
        return eventService.getAllReportedEvents();
    }

}
