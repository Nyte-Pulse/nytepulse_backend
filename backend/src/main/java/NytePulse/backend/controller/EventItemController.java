package NytePulse.backend.controller;


import NytePulse.backend.service.centralServices.EventItemsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event-items")
@RequiredArgsConstructor
public class EventItemController {

    private final EventItemsService eventItemsService;

    @PostMapping("/init")
    public ResponseEntity<?> init() {
        return eventItemsService.initializeData();
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getByCategory(@PathVariable String category) {
        return eventItemsService.getItemsByCategory(category);
    }

}
