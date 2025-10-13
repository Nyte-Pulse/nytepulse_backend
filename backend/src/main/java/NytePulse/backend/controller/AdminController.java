package NytePulse.backend.controller;


import NytePulse.backend.service.centralServices.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private EventService eventService;

    @PostMapping("/event/disableReportedEventByAdmin/{eventId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> disableReportedEventByAdmin(@PathVariable String eventId) {
        return eventService.disableReportedEventByAdmin(eventId);
    }
}
