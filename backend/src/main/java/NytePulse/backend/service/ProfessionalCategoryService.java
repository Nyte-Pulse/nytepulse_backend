package NytePulse.backend.service;

import NytePulse.backend.dto.ProfessionalCategoryDTO;
import NytePulse.backend.dto.ProfessionalTypeDTO;
import NytePulse.backend.entity.ProfessionalCategory;
import NytePulse.backend.entity.ProfessionalType;
import NytePulse.backend.repository.ProfessionalCategoryRepository;
import NytePulse.backend.repository.ProfessionalTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessionalCategoryService {

    private final ProfessionalCategoryRepository categoryRepository;
    private final ProfessionalTypeRepository typeRepository;

    @Transactional(readOnly = true)
    public List<ProfessionalCategoryDTO> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProfessionalCategoryDTO> getAllCategoriesWithTypes() {
        return categoryRepository.findAllActiveWithTypes()
                .stream()
                .map(this::convertToCategoryDTOWithTypes)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProfessionalTypeDTO> getTypesByCategory(Long categoryId) {
        return typeRepository.findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId)
                .stream()
                .map(this::convertToTypeDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void initializeData() {
        if (categoryRepository.count() > 0) {
            log.info("Professional categories already initialized");
            return;
        }

        log.info("Initializing professional categories and types...");

        // 1. Nightclub/Venue
        ProfessionalCategory nightclubVenue = createCategory(
                "Nightclub/Venue", "NIGHTCLUB_VENUE",
                "Venues and establishments for nightlife events", 1
        );
        addType(nightclubVenue, "Nightclub", "NIGHTCLUB", 1);
        addType(nightclubVenue, "Casino", "CASINO", 2);
        addType(nightclubVenue, "Bar/Lounge", "BAR_LOUNGE", 3);
        addType(nightclubVenue, "Event Space/Hall", "EVENT_SPACE", 4);
        addType(nightclubVenue, "Rooftop Bar", "ROOFTOP_BAR", 5);
        addType(nightclubVenue, "Concert Venue", "CONCERT_VENUE", 6);
        categoryRepository.save(nightclubVenue);

        // 2. DJ/Music Producer
        ProfessionalCategory djProducer = createCategory(
                "DJ/Music Producer", "DJ_PRODUCER",
                "Music professionals and audio specialists", 2
        );
        addType(djProducer, "DJ (Club/Resident)", "DJ_CLUB_RESIDENT", 1);
        addType(djProducer, "Music Producer", "MUSIC_PRODUCER", 2);
        addType(djProducer, "Vocalist/MC", "VOCALIST_MC", 3);
        addType(djProducer, "Audio Engineer/Sound Tech", "AUDIO_ENGINEER", 4);
        addType(djProducer, "Record Label", "RECORD_LABEL", 5);
        categoryRepository.save(djProducer);

        // 3. Promoter/Host
        ProfessionalCategory promoter = createCategory(
                "Promoter/Host", "PROMOTER_HOST",
                "Event promotion and hosting professionals", 3
        );
        addType(promoter, "Party Promoter", "PARTY_PROMOTER", 1);
        addType(promoter, "Event Organizer", "EVENT_ORGANIZER", 2);
        addType(promoter, "Guestlist Host", "GUESTLIST_HOST", 3);
        addType(promoter, "VIP Bottle Service Host", "VIP_HOST", 4);
        addType(promoter, "Marketing/PR Agency", "MARKETING_PR", 5);
        categoryRepository.save(promoter);

        // 4. Performer/Artist
        ProfessionalCategory performer = createCategory(
                "Performer/Artist", "PERFORMER_ARTIST",
                "Stage performers and visual artists", 4
        );
        addType(performer, "Dancer (Gogo/Stage)", "DANCER", 1);
        addType(performer, "Visual Artist/VJ (Visual Jockey)", "VJ", 2);
        addType(performer, "Costume Designer", "COSTUME_DESIGNER", 3);
        addType(performer, "Fire/Specialty Performer", "SPECIALTY_PERFORMER", 4);
        categoryRepository.save(performer);

        // 5. Content Creator/Influencer
        ProfessionalCategory creator = createCategory(
                "Content Creator/Influencer", "CONTENT_CREATOR",
                "Digital content and social media professionals", 5
        );
        addType(creator, "Nightlife Photographer", "PHOTOGRAPHER", 1);
        addType(creator, "Videographer/Filmmaker", "VIDEOGRAPHER", 2);
        addType(creator, "Fashion/Style Influencer", "FASHION_INFLUENCER", 3);
        addType(creator, "Party Reviewer/Blogger", "REVIEWER_BLOGGER", 4);
        addType(creator, "Digital Creator (General)", "DIGITAL_CREATOR", 5);
        categoryRepository.save(creator);

        // 6. Service
        ProfessionalCategory service = createCategory(
                "Service", "SERVICE",
                "Hospitality and event service professionals", 6
        );
        addType(service, "Bartender/Mixologist", "BARTENDER", 1);
        addType(service, "Security/Bouncer", "SECURITY", 2);
        addType(service, "Talent Booker/Agent", "TALENT_BOOKER", 3);
        addType(service, "Merchandise/Apparel Brand", "MERCHANDISE", 4);
        categoryRepository.save(service);

        // 7. Other
        ProfessionalCategory other = createCategory(
                "Other", "OTHER",
                "Other professional categories", 7
        );
        categoryRepository.save(other);

        log.info("Professional categories initialized successfully");
    }

    private ProfessionalCategory createCategory(String name, String code, String description, int order) {
        return ProfessionalCategory.builder()
                .categoryName(name)
                .categoryCode(code)
                .description(description)
                .displayOrder(order)
                .isActive(true)
                .build();
    }

    private void addType(ProfessionalCategory category, String name, String code, int order) {
        ProfessionalType type = ProfessionalType.builder()
                .category(category)
                .typeName(name)
                .typeCode(code)
                .displayOrder(order)
                .isActive(true)
                .build();
        category.getProfessionalTypes().add(type);
    }

    private ProfessionalCategoryDTO convertToCategoryDTO(ProfessionalCategory category) {
        return ProfessionalCategoryDTO.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .categoryCode(category.getCategoryCode())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .build();
    }

    private ProfessionalCategoryDTO convertToCategoryDTOWithTypes(ProfessionalCategory category) {
        List<ProfessionalTypeDTO> types = category.getProfessionalTypes().stream()
                .filter(ProfessionalType::getIsActive)
                .sorted((a, b) -> a.getDisplayOrder().compareTo(b.getDisplayOrder()))
                .map(this::convertToTypeDTO)
                .collect(Collectors.toList());

        return ProfessionalCategoryDTO.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .categoryCode(category.getCategoryCode())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .types(types)
                .build();
    }

    private ProfessionalTypeDTO convertToTypeDTO(ProfessionalType type) {
        return ProfessionalTypeDTO.builder()
                .id(type.getId())
                .categoryId(type.getCategory().getId())
                .typeName(type.getTypeName())
                .typeCode(type.getTypeCode())
                .description(type.getDescription())
                .displayOrder(type.getDisplayOrder())
                .build();
    }
}
