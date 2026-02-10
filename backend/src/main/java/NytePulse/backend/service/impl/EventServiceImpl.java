package NytePulse.backend.service.impl;

import NytePulse.backend.dto.*;
import NytePulse.backend.entity.*;
import NytePulse.backend.repository.*;
import NytePulse.backend.service.BunnyNetService;
import NytePulse.backend.service.centralServices.EventService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

            if (eventDetailsDto.getOrganizerDetails() != null && !eventDetailsDto.getOrganizerDetails().isEmpty()) {
                for (OrganizerDetailDto orgDto : eventDetailsDto.getOrganizerDetails()) {
                    EventOrganizer organizer = new EventOrganizer();
                    organizer.setOrganizerName(orgDto.getOrganizerName());
                    organizer.setOrganizerContact(orgDto.getOrganizerContact());
                    organizer.setOrganizerEmail(orgDto.getOrganizerEmail());
                    organizer.setWebsiteUrl(orgDto.getWebsiteUrl());

                    if (orgDto.getUserId() != null) {
                        if (clubDetailsRepository.existsById(orgDto.getUserId())) {
                            organizer.setUserId(orgDto.getUserId());
                        }
                    }

                    eventDetails.addOrganizer(organizer);
                }
            }


            eventDetails.setName(eventDetailsDto.getName());
            eventDetails.setClubId(eventDetailsDto.getUserId());
            eventDetails.setDescription(eventDetailsDto.getDescription());
            eventDetails.setCategory(eventDetailsDto.getCategory());
            eventDetails.setStartDateTime(eventDetailsDto.getStartDateTime());
            eventDetails.setEndDateTime(eventDetailsDto.getEndDateTime());
            eventDetails.setAgeRestriction(eventDetailsDto.getAgeRestriction());
            eventDetails.setDressCode(eventDetailsDto.getDressCode());
            eventDetails.setTicketType(eventDetailsDto.getTicketType());
            eventDetails.setClubName(eventDetailsDto.getClubName());
            eventDetails.setWebsiteUrl(eventDetailsDto.getWebsiteUrl());
            eventDetails.setPosterUrl(eventDetails.getEventPosterCdnUrl());
            eventDetails.setEventPosterFileName(eventDetailsDto.getEventPosterFileName());
            eventDetails.setEventPosterCdnUrl(eventDetailsDto.getEventPosterCdnUrl());
            eventDetails.setStatus(eventDetailsDto.getStatus());
            eventDetails.setHighlightTags(eventDetailsDto.getHighlightTags());
            eventDetails.setAddress(eventDetailsDto.getVenueAddress());
            eventDetails.setLongitude(eventDetailsDto.getVenueLongitude());
            eventDetails.setLatitude(eventDetailsDto.getVenueLatitude());
//            eventDetails.setLocationName(eventDetailsDto.getVenueName());
            eventDetails.setCity(eventDetailsDto.getVenueCity());
            eventDetails.setVenueName(eventDetailsDto.getVenueName());
            eventDetails.setVenueAddress(eventDetailsDto.getVenueAddress());
            eventDetails.setVenueCity(eventDetailsDto.getVenueCity());
            eventDetails.setAmenities(eventDetailsDto.getAmenities());
            eventDetails.setParking(eventDetailsDto.getParking());
            eventDetails.setSpecialOffers(eventDetailsDto.getSpecialOffers());
            eventDetails.setCurrency(eventDetailsDto.getCurrency());
            eventDetails.setTicketLink(eventDetailsDto.getTicketLink());
            eventDetails.setTicketPrice(eventDetailsDto.getTicketPrice());
//            eventDetails.setOrganizer(organizer);
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
            response.put("venueName", savedEventDetails.getVenueName());
            response.put("venueAddress", savedEventDetails.getVenueAddress());
            response.put("venueCity", savedEventDetails.getVenueCity());
            response.put("category", savedEventDetails.getCategory());
            response.put("startDateTime", savedEventDetails.getStartDateTime());
            response.put("endDateTime", savedEventDetails.getEndDateTime());
            response.put("ageRestriction", savedEventDetails.getAgeRestriction());
            response.put("dressCode", savedEventDetails.getDressCode());
            response.put("eventPosterUrl", savedEventDetails.getEventPosterCdnUrl());
//            response.put("organizer", savedEventDetails.getOrganizer());
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


    @Override
    public ResponseEntity<?> updateEvent(EventDetailsDto eventDetailsDto, String eventId) {
        try {
            if (eventDetailsDto == null || !StringUtils.hasText(eventId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid request");
                errorResponse.put("message", "Request body cannot be null and eventId must be provided");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            EventDetails existingEvent = eventDetailsRepository.findByEventId(eventId);

            if (existingEvent == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Event not found");
                errorResponse.put("eventId", eventId);
                errorResponse.put("message", "No event found for the provided eventId");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            if (eventDetailsDto.getOrganizerDetails() != null) {

                // Initialize list if null, otherwise clear existing to replace them
                if (existingEvent.getOrganizers() == null) {
                    existingEvent.setOrganizers(new ArrayList<>());
                } else {
                    existingEvent.getOrganizers().clear();
                }

                for (OrganizerDetailDto orgDto : eventDetailsDto.getOrganizerDetails()) {
                    EventOrganizer organizer = new EventOrganizer();
                    organizer.setOrganizerName(orgDto.getOrganizerName());
                    organizer.setOrganizerContact(orgDto.getOrganizerContact());
                    organizer.setOrganizerEmail(orgDto.getOrganizerEmail());
                    organizer.setWebsiteUrl(orgDto.getWebsiteUrl());

                    // Validate User/Club ID existence
                    if (orgDto.getUserId() != null) {
                        if (clubDetailsRepository.existsById(orgDto.getUserId())) {
                            organizer.setUserId(orgDto.getUserId());
                        }
                    }

                    // Add to parent entity
                    existingEvent.addOrganizer(organizer);
                }
            }

            existingEvent.setName(eventDetailsDto.getName());
            existingEvent.setDescription(eventDetailsDto.getDescription());
            existingEvent.setCategory(eventDetailsDto.getCategory());
            existingEvent.setStartDateTime(eventDetailsDto.getStartDateTime());
            existingEvent.setEndDateTime(eventDetailsDto.getEndDateTime());
            existingEvent.setAgeRestriction(eventDetailsDto.getAgeRestriction());
            existingEvent.setDressCode(eventDetailsDto.getDressCode());
            existingEvent.setTicketType(eventDetailsDto.getTicketType());

            if (eventDetailsDto.getUserId() != null) {
                existingEvent.setClubId(eventDetailsDto.getUserId());
            }
            existingEvent.setClubName(eventDetailsDto.getClubName());

            existingEvent.setStatus(eventDetailsDto.getStatus());
            existingEvent.setHighlightTags(eventDetailsDto.getHighlightTags());
            existingEvent.setWebsiteUrl(eventDetailsDto.getWebsiteUrl());


            // Consistent with saveEvent: set CDN URL, Filename, and map PosterUrl to the CDN URL
            existingEvent.setEventPosterFileName(eventDetailsDto.getEventPosterFileName());
            existingEvent.setEventPosterCdnUrl(eventDetailsDto.getEventPosterCdnUrl());
            // In saveEvent, you set PosterUrl to the CDN URL
            existingEvent.setPosterUrl(eventDetailsDto.getEventPosterCdnUrl());

            existingEvent.setAddress(eventDetailsDto.getVenueAddress());
            existingEvent.setLongitude(eventDetailsDto.getVenueLongitude());
            existingEvent.setLatitude(eventDetailsDto.getVenueLatitude());
            existingEvent.setCity(eventDetailsDto.getVenueCity());       // Maps VenueCity to City (as per saveEvent)
            existingEvent.setVenueName(eventDetailsDto.getVenueName());
            existingEvent.setVenueAddress(eventDetailsDto.getVenueAddress());
            existingEvent.setVenueCity(eventDetailsDto.getVenueCity());
            existingEvent.setLocationName(eventDetailsDto.getLocationName());

            existingEvent.setAmenities(eventDetailsDto.getAmenities());
            existingEvent.setParking(eventDetailsDto.getParking());
            existingEvent.setSpecialOffers(eventDetailsDto.getSpecialOffers());
            existingEvent.setCurrency(eventDetailsDto.getCurrency());
            existingEvent.setTicketLink(eventDetailsDto.getTicketLink());
            existingEvent.setTicketPrice(eventDetailsDto.getTicketPrice());
            existingEvent.setIsApprovedByOrganizer(eventDetailsDto.getIsApprovedByOrganizer());

            existingEvent.setUpdatedAt(LocalDateTime.now(SRI_LANKA_ZONE));

            EventDetails savedEventDetails = eventDetailsRepository.save(existingEvent);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event updated successfully");
            response.put("eventId", savedEventDetails.getEventId());
            response.put("name", savedEventDetails.getName());
            response.put("clubId", savedEventDetails.getClubId());
            response.put("description", savedEventDetails.getDescription());
            response.put("venueName", savedEventDetails.getVenueName());
            response.put("venueAddress", savedEventDetails.getVenueAddress());
            response.put("venueCity", savedEventDetails.getVenueCity());
            response.put("category", savedEventDetails.getCategory());
            response.put("startDateTime", savedEventDetails.getStartDateTime());
            response.put("endDateTime", savedEventDetails.getEndDateTime());
            response.put("ageRestriction", savedEventDetails.getAgeRestriction());
            response.put("dressCode", savedEventDetails.getDressCode());

            response.put("eventPosterUrl", savedEventDetails.getEventPosterCdnUrl());
            response.put("eventPosterFileName", savedEventDetails.getEventPosterFileName());
            response.put("posterUrl", savedEventDetails.getPosterUrl());

            response.put("organizerDetails", savedEventDetails.getOrganizers());


            response.put("ticketType", savedEventDetails.getTicketType());
            response.put("websiteUrl", savedEventDetails.getWebsiteUrl());
            response.put("status", savedEventDetails.getStatus());
            response.put("highlightTags", savedEventDetails.getHighlightTags());
            response.put("updated_at", savedEventDetails.getUpdatedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating event details", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error updating event details");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @Override
    public ResponseEntity<?> getEventsByUser(String clubId,int page,int size) {
        try {
            Pageable pageable = Pageable.ofSize(size).withPage(page);
            Page<EventDetails> events = eventDetailsRepository.findByClubId(clubId,pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Events retrieved successfully");
            response.put("events", events);
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving events for clubId: {}", clubId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error retrieving events");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    public ResponseEntity<?> searchEvents(EventDetailsDto eventDetailsDto) {
        try {
            logger.info("Searching events with criteria: name={}, location={}, dateFilter={}, category={}, ticketType={}",
                    eventDetailsDto.getName(), eventDetailsDto.getLocationName(), eventDetailsDto.getDateFilter(),
                    eventDetailsDto.getCategory(), eventDetailsDto.getTicketType());

            // Normalize empty strings to null
            String name = StringUtils.hasText(eventDetailsDto.getName()) ? eventDetailsDto.getName().trim() : null;
            String city = StringUtils.hasText(eventDetailsDto.getCity()) ? eventDetailsDto.getCity().trim() : null;
            String category = StringUtils.hasText(eventDetailsDto.getCategory()) ? eventDetailsDto.getCategory().trim() : null;
            String ticketType = StringUtils.hasText(eventDetailsDto.getTicketType()) ? eventDetailsDto.getTicketType().trim() : null;
            String amenities = StringUtils.hasText(eventDetailsDto.getAmenities()) ? eventDetailsDto.getAmenities().trim() : null;
            String dressCode = StringUtils.hasText(eventDetailsDto.getDressCode()) ? eventDetailsDto.getDressCode().trim() : null;
            String ageRestriction = StringUtils.hasText(eventDetailsDto.getAgeRestriction()) ? eventDetailsDto.getAgeRestriction().trim() : null;

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
                    name, city, startDateTime, endDateTime, category, ticketType,amenities,
                    dressCode,
                    ageRestriction
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

            if (savedEvents.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "No saved events found");
                response.put("totalCount", 0);
                response.put("savedEvents", Collections.emptyList());
                response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
                return ResponseEntity.ok(response);
            }

            List<String> eventIds = savedEvents.stream()
                    .map(SaveEvent::getEventId)
                    .collect(Collectors.toList());


            List<EventDetails> eventDetailsList = eventDetailsRepository.findByEventIdIn(eventIds);


            Map<String, SaveEvent> saveEventMap = savedEvents.stream()
                    .collect(Collectors.toMap(SaveEvent::getEventId, se -> se));


            List<Map<String, Object>> mergedEvents = eventDetailsList.stream()
                    .map(event -> {
                        Map<String, Object> eventWithSaveData = new HashMap<>();

                        eventWithSaveData.put("eventDetails", event);

                        SaveEvent saveEvent = saveEventMap.get(event.getEventId());
                        if (saveEvent != null) {
                            eventWithSaveData.put("savedEventId", saveEvent.getId());
                            eventWithSaveData.put("savedUserId", saveEvent.getUserId());
                            eventWithSaveData.put("savedAt", saveEvent.getCreatedAt());
                        }

                        return eventWithSaveData;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Saved events retrieved successfully");
            response.put("totalCount", mergedEvents.size());
            response.put("savedEvents", mergedEvents);
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
    public ResponseEntity<?> getAllEvents(int page,int size) {
        try {
            Pageable pageable = Pageable.ofSize(size).withPage(page);
            Page<EventDetails> pageResult = eventDetailsRepository.findAll(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "All events retrieved successfully");
            response.put("totalItems", pageResult.getTotalElements());
            response.put("totalPages", pageResult.getTotalPages());
            response.put("currentPage", pageResult.getNumber());
            response.put("events",pageResult.getContent());
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

    @Override
    public ResponseEntity<?> generateEventShareLink(String eventId) {

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

            String baseUrl = "https://nytepulse.com/events/"; // Replace with actual base URL
            String shareLink = baseUrl + eventDetails.getEventId();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event share link generated successfully");
            response.put("status", HttpStatus.OK.value());
            response.put("eventId", eventDetails.getEventId());
            response.put("shareLink", shareLink);
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error generating share link for eventId: {}", eventId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating share link");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @Override
    public ResponseEntity<?> deleteEvent(String eventId){
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

            eventDetailsRepository.delete(eventDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event deleted successfully");
            response.put("eventId", eventId);
            response.put("status", HttpStatus.OK.value());
            response.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting event for eventId: {}", eventId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error deleting event");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now(SRI_LANKA_ZONE));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteExpiredEvents() {
        Date now = new Date();
        eventDetailsRepository.deleteByEndDateTimeBefore(now);

        System.out.println("Cleanup Task: Expired events deleted at " + now);
    }

}
