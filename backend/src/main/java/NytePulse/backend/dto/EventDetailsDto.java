package NytePulse.backend.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class EventDetailsDto {

    private String name;
    private String userId;
    private String description;

    private List<OrganizerDetailDto> organizerDetails;
    private String dateFilter;
    private String category;
    private Date startDateTime;// "any", "today", "tomorrow", "this_week", "this_weekend"
    private Date endDateTime;
    private String ageRestriction;
    private String dressCode;
    private String ticketType;

    private String organizerwebsiteUrl;

    private String clubName;
    private String posterUrl;
    private String status;
    private String highlightTags;
    private Long venueId;

    private String venueName;
    private String venueAddress;
    private String venueCity;
    private Double venueLongitude;
    private Double venueLatitude;

    private String organizerName;

    private String organizerContact;

    private int isApprovedByOrganizer;

    private String organizerEmail;

    private String websiteUrl;

    private String locationName;

    private int isActive;

    private String eventPosterCdnUrl;
    private String eventPosterFileName;
    private  LocalDateTime createdAt;

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }


    public String getDateFilter() {
        return dateFilter;
    }

    public void setDateFilter(String dateFilter) {
        this.dateFilter = dateFilter;
    }

    public String getEventPosterCdnUrl() {
        return eventPosterCdnUrl;
    }
    public void setEventPosterCdnUrl(String eventPosterCdnUrl) {
        this.eventPosterCdnUrl = eventPosterCdnUrl;
    }
    public String getEventPosterFileName() {
        return eventPosterFileName;
    }
    public void setEventPosterFileName(String eventPosterFileName) {
        this.eventPosterFileName = eventPosterFileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getAgeRestriction() {
        return ageRestriction;
    }

    public void setAgeRestriction(String ageRestriction) {
        this.ageRestriction = ageRestriction;
    }

    public String getDressCode() {
        return dressCode;
    }

    public void setDressCode(String dressCode) {
        this.dressCode = dressCode;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHighlightTags() {
        return highlightTags;
    }

    public void setHighlightTags(String highlightTags) {
        this.highlightTags = highlightTags;
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }

    public String getVenueCity() {
        return venueCity;
    }

    public void setVenueCity(String venueCity) {
        this.venueCity = venueCity;
    }

    public Double getVenueLongitude() {
        return venueLongitude;
    }

    public void setVenueLongitude(Double venueLongitude) {
        this.venueLongitude = venueLongitude;
    }

    public Double getVenueLatitude() {
        return venueLatitude;
    }

    public void setVenueLatitude(Double venueLatitude) {
        this.venueLatitude = venueLatitude;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getOrganizerContact() {
        return organizerContact;
    }

    public void setOrganizerContact(String organizerContact) {
        this.organizerContact = organizerContact;
    }

    public String getOrganizerEmail() {
        return organizerEmail;
    }

    public void setOrganizerEmail(String organizerEmail) {
        this.organizerEmail = organizerEmail;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getIsActive() {
        return isActive;
    }
    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }
    public int getIsApprovedByOrganizer() {
        return isApprovedByOrganizer;
    }
    public void setIsApprovedByOrganizer(int isApprovedByOrganizer) {
        this.isApprovedByOrganizer = isApprovedByOrganizer;
    }

    public String getOrganizerwebsiteUrl() {
        return organizerwebsiteUrl;
    }

    public void setOrganizerwebsiteUrl(String organizerwebsiteUrl) {
        this.organizerwebsiteUrl = organizerwebsiteUrl;
    }

    public List<OrganizerDetailDto> getOrganizerDetails() {
        return organizerDetails;
    }

    public void setOrganizerDetails(List<OrganizerDetailDto> organizerDetails) {
        this.organizerDetails = organizerDetails;
    }
}
