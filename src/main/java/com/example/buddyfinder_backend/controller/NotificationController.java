package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.NotificationResponse;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    /**
     * Lấy tất cả notifications của user hiện tại
     * GET /api/notifications
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Lấy unread notifications
     * GET /api/notifications/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Đếm unread notifications (cho badge)
     * GET /api/notifications/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Mark notification là đã đọc
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        NotificationResponse notification = notificationService.markAsRead(id, userId);
        if (notification == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(notification);
    }

    /**
     * Mark tất cả notifications là đã đọc
     * PUT /api/notifications/read-all
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    /**
     * Delete notification
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }

    /**
     * Extract userId from JWT token
     */
    private Long extractUserId(String token) {
        String jwt = token.replace("Bearer ", "");
        return jwtUtil.extractUserId(jwt);
    }
}
