package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.EventDetailsDto;
import NytePulse.backend.dto.SaveEventDto;
import NytePulse.backend.dto.ReportEventDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface EventService {

    ResponseEntity<?> saveEvent(EventDetailsDto eventDetailsDto);

    ResponseEntity<?> searchEvents(EventDetailsDto eventDetailsDto);

    ResponseEntity<?> getEventById(String eventId);

    ResponseEntity<?> saveEventByUser(SaveEventDto saveEventDto);

    ResponseEntity<?> getSavedEventsByUser(String userId);

    ResponseEntity<?> reportEvent(ReportEventDto reportEventDto);

    ResponseEntity<?> getAllReportedEvents();

    ResponseEntity<?> disableReportedEventByAdmin(String eventId);

    ResponseEntity<?> removeSavedEventByUser(Long id);

    ResponseEntity<?> uploadEventPoster(MultipartFile file,String eventId);

    ResponseEntity<?> updateEventPoster(MultipartFile file, String eventId, String oldPosterUrl);

    ResponseEntity<?> getAllEvents();

    ResponseEntity<?> getNotApprovedEvents();

    ResponseEntity<?> approveOrDeclineByOrganizer(Long eventId,boolean isApproved);
}
