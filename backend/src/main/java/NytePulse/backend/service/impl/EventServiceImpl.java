package NytePulse.backend.service.impl;

import NytePulse.backend.dto.BunnyNetUploadResult;
import NytePulse.backend.dto.EventDetailsDto;
import NytePulse.backend.dto.SaveEventDto;
import NytePulse.backend.dto.ReportEventDto;
import NytePulse.backend.entity.*;
import NytePulse.backend.repository.*;
import NytePulse.backend.service.BunnyNetService;
import NytePulse.backend.service.centralServices.EventService;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.*;
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
    private ClubDetailsRepository clubDetailsRepository;

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

            ClubDetails organizer = clubDetailsRepository.getReferenceById(eventDetailsDto.getOrganizerId());
            eventDetails.setOrganizer(organizer);

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
            eventDetails.setPosterUrl(eventDetails.getEventPosterCdnUrl());
            eventDetails.setEventPosterFileName(eventDetailsDto.getEventPosterFileName());
            eventDetails.setEventPosterCdnUrl(eventDetailsDto.getEventPosterCdnUrl());
            eventDetails.setStatus(eventDetailsDto.getStatus());
            eventDetails.setHighlightTags(eventDetailsDto.getHighlightTags());
            eventDetails.setAddress(eventDetailsDto.getVenueAddress());
            eventDetails.setLongitude(eventDetailsDto.getVenueLongitude());
            eventDetails.setLatitude(eventDetailsDto.getVenueLatitude());
            eventDetails.setName(eventDetailsDto.getVenueName());
            eventDetails.setCity(eventDetailsDto.getVenueCity());
            eventDetails.setOrganizer(organizer);
            eventDetails.setIsApprovedByOrganizer(eventDetailsDto.getIsApprovedByOrganizer());
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
            response.put("eventPosterUrl", savedEventDetails.getEventPosterCdnUrl());
            response.put("organizer", savedEventDetails.getOrganizer());
            response.put("eventPosterFileName", savedEventDetails.getEventPosterFileName());
            response.put("ticketType", savedEventDetails.getTicketType());
            response.put("websiteUrl", savedEventDetails.getWebsiteUrl());
            response.put("posterUrl", savedEventDetails.getPosterUrl());
            response.put("status", savedEventDetails.getStatus());
            response.put("highlightTags", savedEventDetails.getHighlightTags());
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
            logger.info("Searching events with criteria: name={}, location={}, dateFilter={}, category={}, ticketType={}",
                    eventDetailsDto.getName(), eventDetailsDto.getLocationName(), eventDetailsDto.getDateFilter(),
                    eventDetailsDto.getCategory(), eventDetailsDto.getTicketType());

            // Normalize empty strings to null
            String name = StringUtils.hasText(eventDetailsDto.getName()) ? eventDetailsDto.getName().trim() : null;
            String location = StringUtils.hasText(eventDetailsDto.getLocationName()) ? eventDetailsDto.getLocationName().trim() : null;
            String category = StringUtils.hasText(eventDetailsDto.getCategory()) ? eventDetailsDto.getCategory().trim() : null;
            String ticketType = StringUtils.hasText(eventDetailsDto.getTicketType()) ? eventDetailsDto.getTicketType().trim() : null;

            // Calculate date range based on filter
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;
            String dateFilter = eventDetailsDto.getDateFilter();

            if (StringUtils.hasText(dateFilter)) {
                LocalDateTime now = LocalDateTime.now(SRI_LANKA_ZONE);

                switch (dateFilter.toLowerCase()) {
                    case "today":
                        startDate = now.toLocalDate().atStartOfDay();
                        endDate = now.toLocalDate().atTime(23, 59, 59);
                        break;

                    case "tomorrow":
                        startDate = now.plusDays(1).toLocalDate().atStartOfDay();
                        endDate = now.plusDays(1).toLocalDate().atTime(23, 59, 59);
                        break;

                    case "this_week":
                        // Get Monday of current week
                        startDate = now.toLocalDate()
                                .with(java.time.DayOfWeek.MONDAY)
                                .atStartOfDay();
                        // Get Sunday of current week
                        endDate = now.toLocalDate()
                                .with(java.time.DayOfWeek.SUNDAY)
                                .atTime(23, 59, 59);
                        break;

                    case "this_weekend":
                        // Get Saturday of current week
                        startDate = now.toLocalDate()
                                .with(java.time.DayOfWeek.SATURDAY)
                                .atStartOfDay();
                        // Get Sunday of current week
                        endDate = now.toLocalDate()
                                .with(java.time.DayOfWeek.SUNDAY)
                                .atTime(23, 59, 59);
                        break;

                    case "any":
                    default:
                        // No date filtering
                        break;
                }
            }

            // Convert LocalDateTime to Date for repository method
            Date startDateTime = startDate != null ?
                    Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant()) : null;
            Date endDateTime = endDate != null ?
                    Date.from(endDate.atZone(ZoneId.systemDefault()).toInstant()) : null;

            List<EventDetails> events = eventDetailsRepository.searchEvents(
                    name, location, startDateTime, endDateTime, category, ticketType
            );

            logger.info("Found {} events matching search criteria", events.size());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Events retrieved successfully");
            response.put("totalCount", events.size());
            response.put("events", events);
            response.put("searchCriteria", eventDetailsDto);
            response.put("appliedDateRange", Map.of(
                    "startDate", startDate != null ? startDate : "N/A",
                    "endDate", endDate != null ? endDate : "N/A"
            ));
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


    public ResponseEntity<?> getEventById(String eventId) {
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

        try {
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

            SaveEvent savedSaveEvent = new SaveEvent();
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
        } catch (Exception e) {
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

            ReportEvent eventSave = new ReportEvent();
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

            if (eventDetails.getIsActive() == 0) {
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

    @Override
    public ResponseEntity<?> removeSavedEventByUser(Long id) {
        try {
            Optional<SaveEvent> findExistingRecord = saveEventByUserRepository.findById(id);
            if (findExistingRecord.isPresent()) {
                saveEventByUserRepository.deleteById(id);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Saved event removed successfully");
                response.put("status", HttpStatus.OK.value());
                response.put("id", id);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Saved event not found");
                errorResponse.put("id", id);
                errorResponse.put("status", HttpStatus.NOT_FOUND.value());
                errorResponse.put("message", "No saved event found for the provided id");
                return ResponseEntity.ok(errorResponse);
            }
        } catch (Exception e) {
            logger.error("No saved event found for the provided id: {}", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error deleting saved event found for the provided id");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.ok(errorResponse);
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("success", "false");
        error.put("error", message);
        return error;
    }

    @Override
    public ResponseEntity<?> uploadEventPoster(MultipartFile file, String eventId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File is empty"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File must be an image"));
            }

            // Validate file size (e.g., max 5MB)
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File size exceeds 5MB limit"));
            }

            logger.info("Uploading event poster");

            BunnyNetUploadResult result = bunnyNetService.uploadEventPoster(file, "event_posters");
            EventDetails eventDetails = eventDetailsRepository.findByEventId(eventId);

            eventDetails.setEventPosterCdnUrl(result.getCdnUrl());
            eventDetails.setEventPosterFileName(result.getFileName());

            eventDetailsRepository.save(eventDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event poster uploaded successfully");
            response.put("fileName", result.getFileName());
            response.put("cdnUrl", result.getCdnUrl());
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error uploading event poster", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error uploading event poster");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> updateEventPoster(MultipartFile file, String eventId, String oldFileName) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File is empty"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File must be an image"));
            }

            // Validate file size (e.g., max 5MB)
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File size exceeds 5MB limit"));
            }

            logger.info("Updating event poster for eventId: {}", eventId);

            BunnyNetUploadResult result = bunnyNetService.updateEventPoster(file, "event_posters", oldFileName);

            EventDetails eventDetails = eventDetailsRepository.findByEventId(eventId);

            eventDetails.setEventPosterCdnUrl(result.getCdnUrl());
            eventDetails.setEventPosterFileName(result.getFileName());

            eventDetailsRepository.save(eventDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event poster updated successfully");
            response.put("fileName", result.getFileName());
            response.put("cdnUrl", result.getCdnUrl());
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating event poster for eventId: {}", eventId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error updating event poster");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> getAllEvents() {
        try {
            List<EventDetails> allEvents = eventDetailsRepository.findAll();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "All events retrieved successfully");
            response.put("totalCount", allEvents.size());
            response.put("events", allEvents);
            response.put("status", HttpStatus.OK.value());
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving all events", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error retrieving all events");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> getNotApprovedEvents() {
        try {
            List<EventDetails> notApprovedEvents = eventDetailsRepository.findByIsApprovedByOrganizer(0);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Not approved events retrieved successfully");
            response.put("totalCount", notApprovedEvents.size());
            response.put("events", notApprovedEvents);
            response.put("status", HttpStatus.OK.value());
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving not approved events", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error retrieving not approved events");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> approveOrDeclineByOrganizer(Long eventId, boolean isApproved) {
        try {
            EventDetails eventDetails = eventDetailsRepository.findById(eventId)
                    .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

            eventDetails.setIsApprovedByOrganizer(isApproved ? 1 : -1);

            eventDetailsRepository.save(eventDetails);

            String action = isApproved ? "approved" : "declined";

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event " + action + " successfully");
            response.put("eventId", eventDetails.getEventId());
            response.put("status", HttpStatus.OK.value());
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            logger.error("Event not found with id: {}", eventId);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Event not found");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            logger.error("Error approving event with id: {}", eventId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error processing event");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
