package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.NotificationResponse;
import com.example.buddyfinder_backend.entity.Notification;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.NotificationRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Tạo và gửi notification (với WebSocket real-time)
     */
    @Transactional
    public NotificationResponse createNotification(
            Long userId,
            Notification.NotificationType type,
            String title,
            String message,
            Long relatedId,
            String relatedType
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created notification for user {}: {}", userId, title);

        // Send real-time notification via WebSocket
        NotificationResponse response = mapToResponse(saved);
        sendRealTimeNotification(userId, response);

        return response;
    }

    /**
     * Gửi notification real-time qua WebSocket
     */
    private void sendRealTimeNotification(Long userId, NotificationResponse notification) {
        try {
            String destination = "/topic/notifications/" + userId;
            messagingTemplate.convertAndSend(destination, notification);
            log.info("Sent real-time notification to: {}", destination);
        } catch (Exception e) {
            log.error("Failed to send real-time notification", e);
        }
    }

    /**
     * Helper method: Tạo MATCH notification
     */
    public void notifyMatch(Long userId, Long matchId, String matchedUserName) {
        createNotification(
                userId,
                Notification.NotificationType.MATCH,
                "New Match!",
                "You matched with " + matchedUserName + "!",
                matchId,
                "MATCH"
        );
    }

    /**
     * Helper method: Tạo MESSAGE notification
     */
    public void notifyNewMessage(Long userId, Long matchId, String senderName) {
        createNotification(
                userId,
                Notification.NotificationType.MESSAGE,
                "New Message",
                senderName + " sent you a message",
                matchId,
                "MATCH"
        );
    }

    public void notifyGroupMessage(Long userId, Long roomId, String senderName, String activityTitle) {
        createNotification(
                userId,
                Notification.NotificationType.MESSAGE,
                "Group Chat",
                senderName + " sent a message in " + (activityTitle != null ? activityTitle : "a group chat"),
                roomId,
                "GROUP"
        );
    }

    /**
     * Helper method: Tạo ACTIVITY notification
     */
    public void notifyActivityJoined(Long userId, Long activityId, String participantName, String activityTitle) {
        createNotification(
                userId,
                Notification.NotificationType.ACTIVITY_JOINED,
                "Activity Update",
                participantName + " joined your activity: " + activityTitle,
                activityId,
                "ACTIVITY"
        );
    }

    /**
     * Helper method: Tạo RATING notification
     */
    public void notifyRatingReceived(Long userId, Long ratingId, String raterName, Integer rating) {
        createNotification(
                userId,
                Notification.NotificationType.RATING_RECEIVED,
                "New Rating",
                raterName + " rated you " + rating + " stars",
                ratingId,
                "RATING"
        );
    }

    /**
     * Lấy tất cả notifications của user
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByUser_UserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy unread notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Đếm unread notifications
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUser_UserIdAndIsReadFalse(userId);
    }

    /**
     * Mark notification là đã đọc
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Security check
        if (!notification.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setIsRead(true);
        Notification updated = notificationRepository.save(notification);

        log.info("Marked notification {} as read", notificationId);
        return mapToResponse(updated);
    }

    /**
     * Mark tất cả notifications là đã đọc
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        log.info("Marked all notifications as read for user {}", userId);
    }

    /**
     * Delete notification
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Security check
        if (!notification.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        notificationRepository.delete(notification);
        log.info("Deleted notification {}", notificationId);
    }

    /**
     * Delete old notifications (cleanup job)
     */
    @Transactional
    public void deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        notificationRepository.deleteOldNotifications(cutoffDate);
        log.info("Cleaned up notifications older than {} days", daysOld);
    }

    /**
     * Map entity to DTO
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notiId(notification.getNotiId())
                .userId(notification.getUser().getUserId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedId(notification.getRelatedId())
                .relatedType(notification.getRelatedType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
