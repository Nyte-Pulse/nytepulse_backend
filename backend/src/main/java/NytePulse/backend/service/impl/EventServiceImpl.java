package NytePulse.backend.service.impl;

import NytePulse.backend.dto.BunnyNetUploadResult;
import NytePulse.backend.dto.EventDetailsDto;
import NytePulse.backend.dto.SaveEventDto;
import NytePulse.backend.dto.ReportEventDto;
import NytePulse.backend.entity.CountEvent;
import NytePulse.backend.entity.EventDetails;
import NytePulse.backend.entity.SaveEvent;
import NytePulse.backend.entity.ReportEvent;
import NytePulse.backend.repository.EventCountRepository;
import NytePulse.backend.repository.EventDetailsRepository;
import NytePulse.backend.repository.ReportEventRepository;
import NytePulse.backend.repository.SaveEventByUserRepository;
import NytePulse.backend.service.BunnyNetService;
import NytePulse.backend.service.centralServices.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventDetailsRepository eventDetailsRepository;

    @Autowired
    private SaveEventByUserRepository saveEventByUserRepository;

    @Autowired
    private EventCountRepository eventCountRepository;

    @Autowired
    private ReportEventRepository reportEventRepository;

    @Autowired
    private BunnyNetService bunnyNetService;

    private static final Logger logger = LoggerFactory.getLogger(ClubServiceImpl.class);

    private static final ZoneId SRI_LANKA_ZONE = ZoneId.of("Asia/Colombo");

    @Override
    public ResponseEntity<?> saveEvent(EventDetailsDto eventDetailsDto) {
        try {

            if (eventDetailsDto == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid request");
                errorResponse.put("message", "Request body cannot be null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            EventDetails eventDetails = new EventDetails();

            String lastEventId = eventDetailsRepository.findLastEventId();
            String newEventId = "EV000001";

            if (lastEventId != null && !lastEventId.isEmpty()) {
                String numericPart = lastEventId.substring(2);
                int lastIdNumber = Integer.parseInt(numericPart);
                newEventId = String.format("EV%06d", lastIdNumber + 1);
            }
            eventDetails.setEventId(newEventId);

//            if (file.isEmpty()) {
//                return ResponseEntity.badRequest()
//                        .body(createErrorResponse("File is empty"));
//            }
//
//            // Validate file type
//            String contentType = file.getContentType();
//            if (contentType == null || !contentType.startsWith("image/")) {
//                return ResponseEntity.badRequest()
//                        .body(createErrorResponse("File must be an image"));
//            }
//
//            // Validate file size (e.g., max 5MB)
//            long maxSize = 5 * 1024 * 1024; // 5MB
//            if (file.getSize() > maxSize) {
//                return ResponseEntity.badRequest()
//                        .body(createErrorResponse("File size exceeds 5MB limit"));
//            }
//
//            logger.info("Uploading profile picture for user: {}", eventDetailsDto.getUserId());
//
//            BunnyNetUploadResult result = bunnyNetService.uploadEventPoster(file, eventDetailsDto.getUserId());


            eventDetails.setName(eventDetailsDto.getName());
            eventDetails.setClubId(eventDetailsDto.getUserId());
            eventDetails.setDescription(eventDetailsDto.getDescription());
            eventDetails.setCategory(eventDetailsDto.getCategory());
            eventDetails.setStartDateTime(eventDetailsDto.getStartDateTime());
            eventDetails.setEndDateTime(eventDetailsDto.getEndDateTime());
            eventDetails.setAgeRestriction(eventDetailsDto.getAgeRestriction());
            eventDetails.setDressCode(eventDetailsDto.getDressCode());
            eventDetails.setTicketType(eventDetailsDto.getTicketType());
            eventDetails.setWebsiteUrl(eventDetailsDto.getWebsiteUrl());
//            eventDetails.setPosterUrl(result.getCdnUrl());
//            eventDetails.setEventPosterFileName(result.getFileName());
            eventDetails.setStatus(eventDetailsDto.getStatus());
            eventDetails.setHighlightTags(eventDetailsDto.getHighlightTags());
            eventDetails.setAddress(eventDetailsDto.getVenueAddress());
            eventDetails.setLongitude(eventDetailsDto.getVenueLongitude());
            eventDetails.setLatitude(eventDetailsDto.getVenueLatitude());
            eventDetails.setName(eventDetailsDto.getVenueName());
            eventDetails.setCity(eventDetailsDto.getVenueCity());
            eventDetails.setOrganizerContact(eventDetailsDto.getOrganizerContact());
            eventDetails.setOrganizerEmail(eventDetailsDto.getOrganizerEmail());
            eventDetails.setOrganizerName(eventDetailsDto.getOrganizerName());
            eventDetails.setLocationName(eventDetailsDto.getLocationName());
            eventDetails.setCreatedAt(LocalDateTime.now(SRI_LANKA_ZONE));
            eventDetails.setIsActive(1);

            EventDetails savedEventDetails = eventDetailsRepository.save(eventDetails);


            // Update CountEvent for the user
            String userId = savedEventDetails.getClubId();

            CountEvent countEvent = eventCountRepository.findByUserId(userId);

            if (countEvent == null) {
                countEvent = new CountEvent();
                countEvent.setUserId(userId);
                countEvent.setEventCount(1L);
                logger.info("Creating new event count record for userId: {}", userId);
            } else {
                countEvent.setEventCount(countEvent.getEventCount() + 1);
                logger.info("Updated event count for userId: {} to {}", userId, countEvent.getEventCount());
            }

            eventCountRepository.save(countEvent);


            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event saved successfully");
            response.put("eventId", savedEventDetails.getEventId());
            response.put("name", savedEventDetails.getName());
            response.put("clubId", savedEventDetails.getClubId());
            response.put("description", savedEventDetails.getDescription());
            response.put("category", savedEventDetails.getCategory());
            response.put("startDateTime", savedEventDetails.getStartDateTime());
            response.put("endDateTime", savedEventDetails.getEndDateTime());
            response.put("ageRestriction", savedEventDetails.getAgeRestriction());
            response.put("dressCode", savedEventDetails.getDressCode());
            response.put("ticketType", savedEventDetails.getTicketType());
            response.put("websiteUrl", savedEventDetails.getWebsiteUrl());
            response.put("posterUrl", savedEventDetails.getPosterUrl());
            response.put("status", savedEventDetails.getStatus());
            response.put("highlightTags", savedEventDetails.getHighlightTags());
//            response.put("fileName", result.getFileName());
//            response.put("cdnUrl", result.getCdnUrl());
            response.put("updated_at", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error saving event details", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving event details: " + e.getMessage());
        }
    }


    public ResponseEntity<?> searchEvents(EventDetailsDto eventDetailsDto) {
        try {
            logger.info("Searching events with criteria: name={}, location={}, startDate={}, endDate={}, category={}, ticketType={}",
                    eventDetailsDto.getName(), eventDetailsDto.getLocationName(), eventDetailsDto.getStartDateTime(),
                    eventDetailsDto.getEndDateTime(), eventDetailsDto.getCategory(), eventDetailsDto.getTicketType());

            // Normalize empty strings to null for proper query handling
            String name = StringUtils.hasText(eventDetailsDto.getName()) ? eventDetailsDto.getName().trim() : null;
            String location = StringUtils.hasText(eventDetailsDto.getLocationName()) ? eventDetailsDto.getLocationName().trim() : null;
            String category = StringUtils.hasText(eventDetailsDto.getCategory()) ? eventDetailsDto.getCategory().trim() : null;
            String ticketType = StringUtils.hasText(eventDetailsDto.getTicketType()) ? eventDetailsDto.getTicketType().trim() : null;

             List<EventDetails> events = eventDetailsRepository.searchEvents(
                 name, location, eventDetailsDto.getStartDateTime(), eventDetailsDto.getEndDateTime(), category, ticketType
             );

            logger.info("Found {} events matching search criteria", events.size());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Events retrieved successfully");
            response.put("totalCount", events.size());
            response.put("events", events);
            response.put("searchCriteria", eventDetailsDto);
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching events", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error searching events");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    public ResponseEntity<?> getEventById(String eventId){
        try {
            if (!StringUtils.hasText(eventId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid eventId");
                errorResponse.put("message", "eventId cannot be null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            EventDetails eventDetails = eventDetailsRepository.findByEventId(eventId);

            if (eventDetails == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Event not found");
                errorResponse.put("eventId", eventId);
                errorResponse.put("message", "No event found for the provided eventId");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event retrieved successfully");
            response.put("event", eventDetails);
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving event details for eventId: {}", eventId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error retrieving event details");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> saveEventByUser(SaveEventDto saveEventDto) {

        try{
            if (saveEventDto == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid request");
                errorResponse.put("message", "Request body cannot be null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            if (!StringUtils.hasText(saveEventDto.getUserId()) || !StringUtils.hasText(saveEventDto.getEventId())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid userId or eventId");
                errorResponse.put("message", "userId and eventId cannot be null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            SaveEvent existingRecord = saveEventByUserRepository.findByUserIdAndEventId(saveEventDto.getUserId(), saveEventDto.getEventId());

            if (existingRecord != null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Event already saved");
                errorResponse.put("message", "This event is already saved by the user");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            SaveEvent savedSaveEvent =new SaveEvent();
            savedSaveEvent.setUserId(saveEventDto.getUserId());
            savedSaveEvent.setEventId(saveEventDto.getEventId());
            savedSaveEvent.setCreatedAt(LocalDateTime.now(SRI_LANKA_ZONE));

            saveEventByUserRepository.save(savedSaveEvent);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event saved successfully");
            response.put("eventId", savedSaveEvent.getEventId());
            response.put("userId", savedSaveEvent.getUserId());
            response.put("createdAt", savedSaveEvent.getCreatedAt());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch(Exception e){
            logger.error("Error saving event by user", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error saving event by user");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

    }

    @Override
    public ResponseEntity<?> getSavedEventsByUser(String userId) {
        try {
            if (!StringUtils.hasText(userId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid userId");
                errorResponse.put("message", "userId cannot be null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            List<SaveEvent> savedEvents = saveEventByUserRepository.findByUserId(userId);

            List<String> eventIds = savedEvents.stream()
                    .map(SaveEvent::getEventId)
                    .collect(Collectors.toList());

            List<EventDetails> eventDetailsList = eventDetailsRepository.findByEventIdIn(eventIds);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Saved events retrieved successfully");
            response.put("totalCount", savedEvents.size());
            response.put("savedEvents", eventDetailsList);
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving saved events for userId: {}", userId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error retrieving saved events");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> reportEvent(ReportEventDto reportEventDto) {
        try {
            if (reportEventDto == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid request");
                errorResponse.put("message", "Request body cannot be null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (!StringUtils.hasText(reportEventDto.getEventId()) || !StringUtils.hasText(reportEventDto.getReporterId()) || !StringUtils.hasText(reportEventDto.getReason())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid eventId, reporterId or reason");
                errorResponse.put("message", "eventId, reporterId and reason cannot be null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            ReportEvent existingReport = reportEventRepository.findByEventIdAndReporterId(reportEventDto.getEventId(), reportEventDto.getReporterId());

            if (existingReport != null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Event already reported");
                errorResponse.put("message", "This event has already been reported by the user");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            ReportEvent eventSave=new ReportEvent();
            eventSave.setEventId(reportEventDto.getEventId());
            eventSave.setReason(reportEventDto.getReason());
            eventSave.setReporterId(reportEventDto.getReporterId());
            eventSave.setStatus(0); // Assuming 0 is the default status for new reports
            eventSave.setCreatedAt(LocalDateTime.now(SRI_LANKA_ZONE));

            reportEventRepository.save(eventSave);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event reported successfully");
            response.put("eventId", eventSave.getEventId());
            response.put("reporterId", eventSave.getReporterId());
            response.put("reason", eventSave.getReason());
            response.put("status", eventSave.getStatus());
            response.put("createdAt", eventSave.getCreatedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error reporting event for eventId: {}", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error reporting event");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> getAllReportedEvents() {
        try {
            List<ReportEvent> reportedEvents = reportEventRepository.findAll();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reported events retrieved successfully");
            response.put("totalCount", reportedEvents.size());
            response.put("reportedEvents", reportedEvents);
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving reported events", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error retrieving reported events");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> disableReportedEventByAdmin(String eventId) {
        try {
            if (!StringUtils.hasText(eventId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid eventId");
                errorResponse.put("message", "eventId cannot be null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            EventDetails eventDetails = eventDetailsRepository.findByEventId(eventId);


            if (eventDetails == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Event not found");
                errorResponse.put("eventId", eventId);
                errorResponse.put("message", "No event found for the provided eventId");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            if(eventDetails.getIsActive()==0){
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Event already disabled");
                errorResponse.put("eventId", eventId);
                errorResponse.put("message", "This event is already disabled");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            eventDetails.setIsActive(0); // Assuming 0 means disabled
            eventDetailsRepository.save(eventDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event disabled successfully");
            response.put("eventId", eventId);
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error disabling event for eventId: {}", eventId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error disabling event");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("success", "false");
        error.put("error", message);
        return error;
    }


}
