package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.entity.*;
import com.example.buddyfinder_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;
    private final RatingRepository ratingRepository;
    private final RefundRepository refundRepository; // ADD THIS
    private final NotificationService notificationService; // ADD THIS
    private final ReportService reportService;
    private final UserService userService;

    public Map<String, Object> getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .count();
        long totalActivities = activityRepository.count();
        long totalMatches = matchRepository.count();
        long totalMessages = messageRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("totalActivities", totalActivities);
        stats.put("totalMatches", totalMatches);
        stats.put("totalMessages", totalMessages);

        return stats;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User banUser(Long userId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getIsAdmin()) {
            throw new RuntimeException("Unauthorized: Not an admin");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(false);
        return userRepository.save(user);
    }

    public User unbanUser(Long userId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getIsAdmin()) {
            throw new RuntimeException("Unauthorized: Not an admin");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(true);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId, Long adminId) {
        verifyAdmin(adminId);
        userService.deleteUserAccount(userId);
    }

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public void deleteActivity(Long activityId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getIsAdmin()) {
            throw new RuntimeException("Unauthorized: Not an admin");
        }

        activityRepository.deleteById(activityId);
    }

    public List<Map<String, Object>> getAllRatings(Long adminId) {
        verifyAdmin(adminId);
        List<Rating> ratings = ratingRepository.findAll();
        return ratings.stream().map(this::mapRatingToResponse).toList();
    }

    public void deleteRating(Long ratingId, Long adminId) {
        verifyAdmin(adminId);
        ratingRepository.deleteById(ratingId);
    }

    // ============ REFUND MANAGEMENT ============

    public List<Map<String, Object>> getAllRefunds(Long adminId) {
        verifyAdmin(adminId);

        List<Refund> refunds = refundRepository.findAll();
        return refunds.stream()
                .map(this::mapRefundToResponse)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPendingRefunds(Long adminId) {
        verifyAdmin(adminId);

        List<Refund> refunds = refundRepository.findByStatusOrderByRequestedAtDesc(
                Refund.RefundStatus.PENDING
        );

        return refunds.stream()
                .map(this::mapRefundToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveRefund(Long refundId, Long adminId, String adminNotes) {
        User admin = verifyAdmin(adminId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        if (refund.getStatus() != Refund.RefundStatus.PENDING) {
            throw new RuntimeException("Only pending refunds can be approved");
        }

        refund.setStatus(Refund.RefundStatus.APPROVED);
        refund.setProcessedAt(LocalDateTime.now());
        refund.setProcessedBy(admin);
        refund.setAdminNotes(adminNotes);

        // Mock refund transaction ID
        refund.setRefundTransId("REFUND-" + System.currentTimeMillis());
        refund.setCompletedAt(LocalDateTime.now());
        refund.setStatus(Refund.RefundStatus.COMPLETED);

        refundRepository.save(refund);

        // Notify user
        notificationService.createNotification(
                refund.getUser().getUserId(),
                Notification.NotificationType.SYSTEM,
                "Refund Approved ✅",
                "Your refund of $" + refund.getOriginalAmount() + " has been approved and processed",
                refundId,
                "REFUND"
        );
    }

    @Transactional
    public void rejectRefund(Long refundId, Long adminId, String adminNotes) {
        User admin = verifyAdmin(adminId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        if (refund.getStatus() != Refund.RefundStatus.PENDING) {
            throw new RuntimeException("Only pending refunds can be rejected");
        }

        refund.setStatus(Refund.RefundStatus.REJECTED);
        refund.setProcessedAt(LocalDateTime.now());
        refund.setProcessedBy(admin);
        refund.setAdminNotes(adminNotes != null ? adminNotes : "Refund request rejected by admin");

        refundRepository.save(refund);

        // Notify user
        notificationService.createNotification(
                refund.getUser().getUserId(),
                Notification.NotificationType.SYSTEM,
                "Refund Rejected ❌",
                "Your refund request has been rejected. " +
                        (adminNotes != null ? "Reason: " + adminNotes : ""),
                refundId,
                "REFUND"
        );
    }

    private User verifyAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getIsAdmin()) {
            throw new RuntimeException("Unauthorized: Not an admin");
        }

        return admin;
    }

    private Map<String, Object> mapRefundToResponse(Refund refund) {
        Map<String, Object> response = new HashMap<>();
        response.put("refundId", refund.getRefundId());
        response.put("userId", refund.getUser().getUserId());
        response.put("userName", refund.getUser().getName());
        response.put("userEmail", refund.getUser().getEmail());
        response.put("originalAmount", refund.getOriginalAmount());
        response.put("refundMethod", refund.getRefundMethod().name());
        response.put("refundType", refund.getRefundType().name());
        response.put("reason", refund.getReason().name());
        response.put("reasonDescription", refund.getReason().getDescription());
        response.put("description", refund.getDescription());
        response.put("status", refund.getStatus().name());
        response.put("requestedAt", refund.getRequestedAt());
        response.put("processedAt", refund.getProcessedAt());
        response.put("completedAt", refund.getCompletedAt());
        response.put("adminNotes", refund.getAdminNotes());
        response.put("processedByName", refund.getProcessedBy() != null ?
                refund.getProcessedBy().getName() : null);
        return response;
    }

    public List<Map<String, Object>> getAllReports(Long adminId) {
        verifyAdmin(adminId);
        return reportService.getAllReports();
    }

    public Map<String, Object> updateReportStatus(Long adminId, Long reportId, Report.ReportStatus status, String adminNotes) {
        verifyAdmin(adminId);
        return reportService.updateStatus(reportId, status, adminNotes);
    }

    @Transactional
    public Map<String, Object> banUserFromReport(Long adminId, Long reportId, int days, String adminNotes) {
        User admin = verifyAdmin(adminId);
        Map<String, Object> reportData = reportService.updateStatus(reportId, Report.ReportStatus.ACTION_TAKEN, adminNotes);
        Map<?, ?> reported = (Map<?, ?>) reportData.get("reported");
        Long reportedId = ((Number) reported.get("userId")).longValue();

        User user = userRepository.findById(reportedId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime banExpires = LocalDateTime.now().plusDays(days);
        user.setIsActive(false);
        user.setBanUntil(banExpires);
        userRepository.save(user);

        notificationService.createNotification(
                user.getUserId(),
                Notification.NotificationType.SYSTEM,
                "Account Suspended",
                "An administrator has suspended your account for " + days + " days due to policy violations.",
                reportId,
                "REPORT"
        );

        return reportData;
    }

    private Map<String, Object> mapRatingToResponse(Rating rating) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", rating.getRatingId());
        dto.put("fromUserId", rating.getFromUser() != null ? rating.getFromUser().getUserId() : null);
        dto.put("fromUserName", rating.getFromUser() != null ? rating.getFromUser().getName() : null);
        dto.put("toUserId", rating.getToUser() != null ? rating.getToUser().getUserId() : null);
        dto.put("toUserName", rating.getToUser() != null ? rating.getToUser().getName() : null);
        dto.put("rating", rating.getRating());
        dto.put("comment", rating.getReview());
        dto.put("createdAt", rating.getCreatedAt());
        return dto;
    }
}
