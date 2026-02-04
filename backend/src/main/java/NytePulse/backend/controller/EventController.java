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
@CrossOrigin(originPatterns = "*")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<?> saveEvent(@RequestBody EventDetailsDto eventDetailsDto){
        return ResponseEntity.ok(eventService.saveEvent(eventDetailsDto));
    }

    @PutMapping("/update/{eventId}")
    public ResponseEntity<?> updateEvent(@RequestBody EventDetailsDto eventDetailsDto,@PathVariable String eventId){
        return ResponseEntity.ok(eventService.updateEvent(eventDetailsDto,eventId));
    }

    @PostMapping("/uploadEventPoster")
    public ResponseEntity<?> uploadEventPoster(@RequestParam("file") MultipartFile file, @RequestParam("eventId") String eventId) {
        return eventService.uploadEventPoster(file,eventId);
    }
    @PostMapping("/updateEventPoster")
    public ResponseEntity<?> updateEventPoster(@RequestParam("file") MultipartFile file, @RequestParam("eventId") String eventId, @RequestParam("oldPosterUrl")String oldPosterUrl) {
        return eventService.updateEventPoster(file, eventId,oldPosterUrl);

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

    @DeleteMapping("/RemoveSaveEventByUser")
    public ResponseEntity<?> removeSavedEventByUser(@RequestParam Long id) {
        return eventService.removeSavedEventByUser(id);
    }

    @GetMapping("/getAllEvents")
    public ResponseEntity<?> getAllEvents(@RequestParam int page,@RequestParam int size) {
        return eventService.getAllEvents(page,size);
    }

    @GetMapping("/getNotApprovedEvents")
    public ResponseEntity<?> getNotApprovedEvents() {
        return eventService.getNotApprovedEvents();
    }

    @PostMapping("/approveOrDeclineByOrganizer/{eventId}")
    public ResponseEntity<?> approveOrDeclineByOrganizer(@PathVariable Long eventId,@RequestParam boolean isApproved) {
        return eventService.approveOrDeclineByOrganizer(eventId,isApproved);
    }

    @PostMapping("/eventShareLink/{eventId}")
    public ResponseEntity<?> generateEventShareLink(@PathVariable String eventId) {
        return eventService.generateEventShareLink(eventId);
    }

    @GetMapping("/getEventsByUser/{clubId}")
    public ResponseEntity<?> getEventsByUser(@PathVariable String clubId,@RequestParam int page,@RequestParam int size) {
        return eventService.getEventsByUser(clubId,page,size);
    }

    @DeleteMapping("/deleteEvent/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable String eventId) {
        return eventService.deleteEvent(eventId);
    }

}
