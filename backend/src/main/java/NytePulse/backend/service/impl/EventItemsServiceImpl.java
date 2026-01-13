package NytePulse.backend.service.impl;

import NytePulse.backend.entity.EventItem;
import NytePulse.backend.repository.EventItemsRepository;
import NytePulse.backend.service.centralServices.EventItemsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventItemsServiceImpl implements EventItemsService {

    private final EventItemsRepository eventItemsRepository;

    @Override
    @Transactional
    public ResponseEntity<?> initializeData() {
        try {
            if (eventItemsRepository.count() > 0) {
                return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body("Data already initialized.");
            }

            saveItems("EVENT_CATEGORY", Arrays.asList("Night club", "Beach club", "Warehouse", "Live Music", "DJ Night", "Roof Top", "Pool Party", "Cultural", "Corporate", "Wedding", "Concert", "Festival"));
            saveItems("AMENITY", Arrays.asList("Food available", "Drinks available", "Outdoor seating", "Smoking area", "WIFI"));
            saveItems("AGE_RESTRICTION", Arrays.asList("All Ages", "18+", "21+", "25+"));
            saveItems("DRESS_CODE", Arrays.asList("Casual", "Smart Casual", "Business Casual", "Formal", "Black Tie"));

            HashMap<String,Object> response= new HashMap<>();
            response.put("message", "Data successfully initialized.");
            response.put("status", HttpStatus.OK.value());


            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    private void saveItems(String category, List<String> values) {
        List<EventItem> items = values.stream()
                .map(v -> new EventItem(null, category, v))
                .collect(Collectors.toList());
        eventItemsRepository.saveAll(items);
    }

    @Override
    public ResponseEntity<?> getItemsByCategory(String category) {
        try {
            List<EventItem> items = eventItemsRepository.findByCategory(category.toUpperCase());

            HashMap<String,Object> response= new HashMap<>();
            response.put("message", "Data successfully fetched.");
            response.put("items", items);
            response.put("status", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching items.");
        }
    }
    
}
