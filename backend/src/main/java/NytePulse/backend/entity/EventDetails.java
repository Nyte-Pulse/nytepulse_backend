package NytePulse.backend.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "event_details")
public class EventDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false)
    private String eventId;


    private String name;
    private String clubId;

    private String clubName;
    private String description;
    private String category;

    @Column(name = "start_date_time")
    private Date startDateTime;

    @Column(name = "end_date_time")
    private Date endDateTime;

    @Column(name = "age_restriction")
    private String ageRestriction;

    @Column(name = "dress_code")
    private String dressCode;

    @Column(name = "ticket_type")
    private String ticketType;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "poster_url")
    private String posterUrl;
    private String status;

    @Column(name = "highlight_tags")
    private String highlightTags;

    private String Address;
    private String City;
    private Double longitude;
    private Double latitude;

    @Column(name = "is_active")
    private int isActive;

    @Column(name = "is_approved_by_organizer")
    private Integer isApprovedByOrganizer;

    private String amenities;

    private String parking;

    private String specialOffers;

    private String currency;

    private String ticketPrice;

    private String ticketLink;


//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "organizer_id", referencedColumnName = "id")
//    private ClubDetails organizer;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private List<EventOrganizer> organizers = new ArrayList<>();

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "event_poster_file_name")
    private String eventPosterFileName;

    @Column(name = "created_at", updatable = false)
    private  LocalDateTime createdAt;

    @Column(name = "event_poster_cdn_url")
    private String eventPosterCdnUrl;

    public void addOrganizer(EventOrganizer organizer) {
        this.organizers.add(organizer);
        organizer.setEvent(this);
    }

    public String getEventPosterCdnUrl() {
        return eventPosterCdnUrl;
    }

    public void setEventPosterCdnUrl(String eventPosterCdnUrl) {
        this.eventPosterCdnUrl = eventPosterCdnUrl;
    }

    public EventDetails() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
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

    public String getEventPosterFileName() {
        return eventPosterFileName;
    }
    public void setEventPosterFileName(String eventPosterFileName) {
        this.eventPosterFileName = eventPosterFileName;
    }

//    public ClubDetails getOrganizer() {
//        return organizer;
//    }
//
//    public void setOrganizer(ClubDetails organizer) {
//        this.organizer = organizer;
//    }

    public Integer getIsApprovedByOrganizer() {
        return isApprovedByOrganizer;
    }

    public void setIsApprovedByOrganizer(Integer isApprovedByOrganizer) {
        this.isApprovedByOrganizer = isApprovedByOrganizer;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public String getParking() {
        return parking;
    }

    public void setParking(String parking) {
        this.parking = parking;
    }

    public String getSpecialOffers() {
        return specialOffers;
    }

    public void setSpecialOffers(String specialOffers) {
        this.specialOffers = specialOffers;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(String ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getTicketLink() {
        return ticketLink;
    }

    public void setTicketLink(String ticketLink) {
        this.ticketLink = ticketLink;
    }

    public List<EventOrganizer> getOrganizers() {
        return organizers;
    }

    public void setOrganizers(List<EventOrganizer> organizers) {
        this.organizers = organizers;
    }
}
