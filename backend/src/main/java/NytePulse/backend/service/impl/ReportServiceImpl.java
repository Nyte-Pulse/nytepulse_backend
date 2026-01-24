package NytePulse.backend.service.impl;

import NytePulse.backend.dto.ReportRequest;
import NytePulse.backend.entity.Post;
import NytePulse.backend.entity.Report;
import NytePulse.backend.entity.User;
import NytePulse.backend.enums.ReportType;
import NytePulse.backend.repository.PostRepository;
import NytePulse.backend.repository.ReportRepository;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.service.centralServices.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Override
    public ResponseEntity<?> createReport(String currentUserId, ReportRequest request) {
        try {
            Optional<User> reporter = userRepository.findByEmail(currentUserId);
            if (reporter == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Reporter not found.");
            }

            Report report = new Report();
            report.setReporter(reporter.get());
            report.setReason(request.getReason());

            if ("USER".equalsIgnoreCase(request.getReportType())) {
                report.setReportType(ReportType.USER);

                User targetUser = userRepository.findByUserId(request.getTargetId());
                if (targetUser == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Target user not found.");
                }

                if (targetUser.getUserId().equals(currentUserId)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You cannot report yourself.");
                }

                report.setReportedUser(targetUser);

            } else if ("POST".equalsIgnoreCase(request.getReportType())) {
                report.setReportType(ReportType.POST);

                try {
                    Long postId = Long.parseLong(request.getTargetId());
                    Optional<Post> targetPostOpt = postRepository.findById(postId);

                    if (targetPostOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Target post not found.");
                    }
                    report.setReportedPost(targetPostOpt.get());

                } catch (NumberFormatException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Post ID format.");
                }

            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Report Type. Use 'USER' or 'POST'.");
            }

            // 3. Save
            reportRepository.save(report);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Report submitted successfully.");
            response.put("status", HttpStatus.CREATED.value());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error submitting report.");
        }
    }

    @Override
    public ResponseEntity<?> getReportedUsers() {
        try {
            List<Report> reports = reportRepository.findByReportedUserIsNotNull();
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (Report r : reports) {
                Map<String, Object> map = new HashMap<>();
                map.put("reportId", r.getId());
                map.put("reason", r.getReason());
                map.put("status", r.getStatus());
                map.put("createdAt", r.getCreatedAt());

                map.put("reporterId", r.getReporter().getUserId());
                map.put("reporterUsername", r.getReporter().getUsername());

                if (r.getReportedUser() != null) {
                    map.put("reportedUserId", r.getReportedUser().getUserId());
                    map.put("reportedUsername", r.getReportedUser().getUsername());
                    map.put("reportedEmail", r.getReportedUser().getEmail());
                }

                responseList.add(map);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("count", responseList.size());
            response.put("reports", responseList);
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching user reports.");
        }
    }

    @Override
    public ResponseEntity<?> getReportedPosts() {
        try {
            List<Report> reports = reportRepository.findByReportedPostIsNotNull();
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (Report r : reports) {
                Map<String, Object> map = new HashMap<>();
                map.put("reportId", r.getId());
                map.put("reason", r.getReason());
                map.put("status", r.getStatus());
                map.put("createdAt", r.getCreatedAt());

                map.put("reporterId", r.getReporter().getUserId());
                map.put("reporterUsername", r.getReporter().getUsername());

                if (r.getReportedPost() != null) {
                    Post p = r.getReportedPost();
                    map.put("reportedPostId", p.getId());
                    map.put("postContent", p.getContent());
                    map.put("postOwnerId", p.getUser().getUserId()); // Valid because Post has @ManyToOne User
                }

                responseList.add(map);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("count", responseList.size());
            response.put("reports", responseList);
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching post reports.");
        }
    }
}