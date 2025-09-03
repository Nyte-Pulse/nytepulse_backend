package NytePulse.backend.service.impl;

import NytePulse.backend.dto.EventDetailsDto;
import NytePulse.backend.entity.EventCount;
import NytePulse.backend.entity.EventDetails;
import NytePulse.backend.repository.EventCountRepository;
import NytePulse.backend.repository.EventDetailsRepository;
import NytePulse.backend.service.centralServices.EventDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Service
public class EventDetailsServiceImpl implements EventDetailsService {

    @Autowired
    private EventDetailsRepository eventDetailsRepository;

    @Autowired
    private EventCountRepository eventCountRepository;

    private static final Logger logger = LoggerFactory.getLogger(ClubDetailsServiceImpl.class);

    private static final ZoneId SRI_LANKA_ZONE = ZoneId.of("Asia/Colombo");

    @Override
    public ResponseEntity<?> saveEvent(EventDetailsDto eventDetailsDto) {
        try {
            EventDetails eventDetails = new EventDetails();

            String lastEventId = eventDetailsRepository.findLastEventId();
            String newEventId = "EV000001";

            if (lastEventId != null && !lastEventId.isEmpty()) {
                String numericPart = lastEventId.substring(2);
                int lastIdNumber = Integer.parseInt(numericPart);
                newEventId = String.format("EV%06d", lastIdNumber + 1);
            }
            eventDetails.setEventId(newEventId);

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
            eventDetails.setPosterUrl(eventDetailsDto.getPosterUrl());
            eventDetails.setStatus(eventDetailsDto.getStatus());
            eventDetails.setHighlightTags(eventDetailsDto.getHighlightTags());
            eventDetails.setVenueId(eventDetailsDto.getVenueId());
            eventDetails.setOrganizerId(eventDetailsDto.getOrganizerId());

            EventDetails savedEventDetails = eventDetailsRepository.save(eventDetails);


            // Update EventCount for the user
            String userId = savedEventDetails.getClubId();
            
            EventCount eventCount = eventCountRepository.findByUserId(userId);

            if (eventCount == null) {
                eventCount = new EventCount();
                eventCount.setUserId(userId);
                eventCount.setEventCount(1L);
                logger.info("Creating new event count record for userId: {}", userId);
            } else {
                eventCount.setEventCount(eventCount.getEventCount() + 1);
                logger.info("Updated event count for userId: {} to {}", userId, eventCount.getEventCount());
            }

            eventCountRepository.save(eventCount);


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
            response.put("venueId", savedEventDetails.getVenueId());
            response.put("organizerId", savedEventDetails.getOrganizerId());
            response.put("updated_at", LocalDateTime.now(SRI_LANKA_ZONE));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error saving event details", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving event details: " + e.getMessage());
        }
    }
}
