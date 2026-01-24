package NytePulse.backend.service.centralServices;
import NytePulse.backend.dto.ReportRequest;
import org.springframework.http.ResponseEntity;

public interface ReportService {
    ResponseEntity<?> createReport(String currentUserId, ReportRequest request);
    ResponseEntity<?> getReportedUsers();
    ResponseEntity<?> getReportedPosts();
}