package NytePulse.backend.repository;

import NytePulse.backend.entity.EventDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface EventDetailsRepository extends JpaRepository<EventDetails,Long>{

    @Query(value = "SELECT event_id FROM event_details WHERE event_id LIKE 'EV%' ORDER BY event_id DESC LIMIT 1", nativeQuery = true)
    String findLastEventId();


    @Query("SELECT e FROM EventDetails e WHERE " +
            "(:name IS NULL OR :name = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:locationName IS NULL OR :locationName = '' OR LOWER(e.locationName) LIKE LOWER(CONCAT('%', :locationName, '%'))) AND " +
            "(:startDateTime IS NULL OR e.startDateTime >= :startDateTime) AND " +
            "(:endDateTime IS NULL OR e.endDateTime <= :endDateTime) AND " +
            "(:category IS NULL OR :category = '' OR LOWER(e.category) = LOWER(:category)) AND " +
            "(:ticketType IS NULL OR :ticketType = '' OR LOWER(e.ticketType) = LOWER(:ticketType)) AND " +
            "(:amenities IS NULL OR :amenities = '' OR LOWER(e.amenities) LIKE LOWER(CONCAT('%', :amenities, '%'))) AND " +
            "(:dressCode IS NULL OR :dressCode = '' OR LOWER(e.dressCode) = LOWER(:dressCode)) AND " +
            // CHANGED THIS LINE BELOW to use LIKE
            "(:ageRestriction IS NULL OR :ageRestriction = '' OR LOWER(e.ageRestriction) LIKE LOWER(CONCAT('%', :ageRestriction, '%'))) " +
            "ORDER BY e.startDateTime ASC")
    List<EventDetails> searchEvents(@Param("name") String name,
                                    @Param("locationName") String locationName,
                                    @Param("startDateTime") Date startDateTime,
                                    @Param("endDateTime") Date endDateTime,
                                    @Param("category") String category,
                                    @Param("ticketType") String ticketType,
                                    @Param("amenities") String amenities,
                                    @Param("dressCode") String dressCode,
                                    @Param("ageRestriction") String ageRestriction);

    EventDetails findByEventId(String eventId);

    List<EventDetails> findByEventIdIn(List<String> eventIds);

    List<EventDetails> findByIsApprovedByOrganizer(int i);

    Page<EventDetails> findByClubId(String clubId, Pageable pageable);
}
