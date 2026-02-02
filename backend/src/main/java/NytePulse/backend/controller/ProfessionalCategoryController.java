package NytePulse.backend.controller;

import NytePulse.backend.dto.ProfessionalCategoryDTO;
import NytePulse.backend.dto.ProfessionalTypeDTO;
import NytePulse.backend.service.ProfessionalCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/professional")
public class ProfessionalCategoryController {

    @Autowired
    private  ProfessionalCategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        List<ProfessionalCategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(Map.of(
                "categories", categories,
                "total", categories.size()
        ));
    }

    @GetMapping("/categories-with-types")
    public ResponseEntity<?> getAllCategoriesWithTypes() {
        List<ProfessionalCategoryDTO> categories = categoryService.getAllCategoriesWithTypes();
        return ResponseEntity.ok(Map.of(
                "categories", categories,
                "total", categories.size()
        ));
    }

    @GetMapping("/categories/{categoryId}/types")
    public ResponseEntity<?> getTypesByCategory(@PathVariable Long categoryId) {
        List<ProfessionalTypeDTO> types = categoryService.getTypesByCategory(categoryId);
        return ResponseEntity.ok(Map.of(
                "types", types,
                "total", types.size()
        ));
    }

    @PostMapping("/initialize")
    public ResponseEntity<?> initializeData() {
        categoryService.initializeData();
        return ResponseEntity.ok(Map.of(
                "message", "Professional categories initialized successfully"
        ));
    }

    @PostMapping("/saveUserProfessionalCategories/{userId}")
    public ResponseEntity<?> saveUserProfessionalCategories(
            @PathVariable Long userId,
            @RequestParam(value = "professionalTypeIds", required = false) List<Long> professionalTypeIds,
            @RequestParam(value = "otherProfessionalType", required = false) String otherProfessionalType) {

        if (professionalTypeIds == null) {
            professionalTypeIds = new ArrayList<>();
        }

        return categoryService.saveUserProfessionalCategories(userId, professionalTypeIds, otherProfessionalType);
    }
}
