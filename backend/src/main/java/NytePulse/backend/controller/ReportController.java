package NytePulse.backend.controller;

import NytePulse.backend.dto.ReportRequest;
import NytePulse.backend.service.centralServices.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/create")
    public ResponseEntity<?> createReport(@RequestBody ReportRequest request, Authentication authentication) {

        String currentUserId = authentication.getName();

        return reportService.createReport(currentUserId, request);
    }

    // Get All User Reports (Admin usually)
    @GetMapping("/users")
    public ResponseEntity<?> getReportedUsers() {
        return reportService.getReportedUsers();
    }

    // Get All Post Reports (Admin usually)
    @GetMapping("/posts")
    public ResponseEntity<?> getReportedPosts() {
        return reportService.getReportedPosts();
    }
}