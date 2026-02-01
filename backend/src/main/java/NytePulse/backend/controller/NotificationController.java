package NytePulse.backend.controller;

import NytePulse.backend.dto.NotificationDTO;
import NytePulse.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDTO> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{userId}/unread")
    public ResponseEntity<?> getUnreadNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{userId}/count")
    public ResponseEntity<?> getUnreadCount(@PathVariable Long userId) {
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId
    ) {
       return notificationService.markAsRead(notificationId, userId);
    }

    @PutMapping("/{userId}/read-all")
    public ResponseEntity<?> markAllAsRead(@PathVariable Long userId) {
       return notificationService.markAllAsRead(userId);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam Long userId
    ) {
        return notificationService.deleteNotification(notificationId, userId);
    }
}
