package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.ProcessRefundRequest;
import com.example.buddyfinder_backend.dto.RefundRequest;
import com.example.buddyfinder_backend.dto.RefundResponse;
import com.example.buddyfinder_backend.entity.Refund;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.RefundRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final RefundRepository refundRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * User request refund
     */
    @Transactional
    public RefundResponse requestRefund(Long userId, RefundRequest request) {
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if already requested refund for this transaction
        if (request.getOriginalTransId() != null &&
                refundRepository.existsByUser_UserIdAndOriginalTransId(userId, request.getOriginalTransId())) {
            throw new RuntimeException("Refund already requested for this transaction");
        }

        // Create refund request
        Refund refund = Refund.builder()
                .user(user)
                .subId(request.getSubId())
                .refundMethod(Refund.RefundMethod.valueOf(request.getRefundMethod()))
                .originalAmount(request.getOriginalAmount())
                .refundType(Refund.RefundType.valueOf(request.getRefundType()))
                .reason(Refund.RefundReason.valueOf(request.getReason()))
                .description(request.getDescription())
                .status(Refund.RefundStatus.PENDING)
                .paymentGateway(request.getPaymentGateway())
                .originalTransId(request.getOriginalTransId())
                .build();

        Refund saved = refundRepository.save(refund);
        log.info("Refund request created: ID={}, User={}, Amount={}",
                saved.getRefundId(), userId, request.getOriginalAmount());

        // Notify user
        notificationService.createNotification(
                userId,
                com.example.buddyfinder_backend.entity.Notification.NotificationType.SYSTEM,
                "Refund Request Submitted",
                "Your refund request has been submitted and is under review",
                saved.getRefundId(),
                "REFUND"
        );

        notifyAdminsAboutNewRefund(saved);

        return mapToResponse(saved);
    }

    /**
     * Admin: Get all pending refunds
     */
    @Transactional(readOnly = true)
    public List<RefundResponse> getPendingRefunds() {
        List<Refund> refunds = refundRepository.findPendingRefunds();
        return refunds.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Admin: Get refund by ID
     */
    @Transactional(readOnly = true)
    public RefundResponse getRefundById(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));
        return mapToResponse(refund);
    }

    /**
     * User: Get own refunds
     */
    @Transactional(readOnly = true)
    public List<RefundResponse> getUserRefunds(Long userId) {
        List<Refund> refunds = refundRepository.findByUser_UserIdOrderByRequestedAtDesc(userId);
        return refunds.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Admin: Process refund (Approve/Reject)
     */
    @Transactional
    public RefundResponse processRefund(Long refundId, Long adminId, ProcessRefundRequest request) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getIsAdmin()) {
            throw new RuntimeException("Unauthorized: User is not admin");
        }

        // Update refund based on action
        if ("APPROVE".equals(request.getAction())) {
            refund.setStatus(Refund.RefundStatus.APPROVED);
            refund.setProcessedAt(LocalDateTime.now());
            refund.setProcessedBy(admin);
            refund.setAdminNotes(request.getAdminNotes());

            log.info("Refund {} approved by admin {}", refundId, adminId);

            // Start processing refund
            processApprovedRefund(refund, request.getRefundTransId());

            // Notify user
            notificationService.createNotification(
                    refund.getUser().getUserId(),
                    com.example.buddyfinder_backend.entity.Notification.NotificationType.SYSTEM,
                    "Refund Approved",
                    "Your refund request has been approved. Processing refund of $" + refund.getOriginalAmount(),
                    refundId,
                    "REFUND"
            );

        } else if ("REJECT".equals(request.getAction())) {
            refund.setStatus(Refund.RefundStatus.REJECTED);
            refund.setProcessedAt(LocalDateTime.now());
            refund.setProcessedBy(admin);
            refund.setAdminNotes(request.getAdminNotes());

            log.info("Refund {} rejected by admin {}", refundId, adminId);

            // Notify user
            notificationService.createNotification(
                    refund.getUser().getUserId(),
                    com.example.buddyfinder_backend.entity.Notification.NotificationType.SYSTEM,
                    "Refund Rejected",
                    "Your refund request has been rejected. Reason: " + request.getAdminNotes(),
                    refundId,
                    "REFUND"
            );
        } else {
            throw new RuntimeException("Invalid action: " + request.getAction());
        }

        Refund updated = refundRepository.save(refund);
        return mapToResponse(updated);
    }

    /**
     * Process approved refund (integrate with payment gateway)
     */
    @Transactional
    private void processApprovedRefund(Refund refund, String refundTransId) {
        try {
            // Update status to PROCESSING
            refund.setStatus(Refund.RefundStatus.PROCESSING);
            refund.setRefundTransId(refundTransId);
            refundRepository.save(refund);

            // TODO: Integrate with actual payment gateway (Stripe, PayPal, etc.)
            // For now, simulate successful refund
            simulatePaymentGatewayRefund(refund);

            // Mark as COMPLETED
            refund.setStatus(Refund.RefundStatus.COMPLETED);
            refund.setCompletedAt(LocalDateTime.now());
            refundRepository.save(refund);

            log.info("Refund {} completed successfully", refund.getRefundId());

            // Notify user
            notificationService.createNotification(
                    refund.getUser().getUserId(),
                    com.example.buddyfinder_backend.entity.Notification.NotificationType.SYSTEM,
                    "Refund Completed 💰",
                    "Your refund of $" + refund.getOriginalAmount() + " has been processed successfully",
                    refund.getRefundId(),
                    "REFUND"
            );

        } catch (Exception e) {
            log.error("Failed to process refund {}: {}", refund.getRefundId(), e.getMessage());
            refund.setStatus(Refund.RefundStatus.UNDER_REVIEW);
            refund.setAdminNotes("Failed to process: " + e.getMessage());
            refundRepository.save(refund);
        }
    }

    /**
     * Simulate payment gateway refund
     * TODO: Replace with actual payment gateway integration
     */
    private void simulatePaymentGatewayRefund(Refund refund) {
        // Simulate delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Simulated refund processed for transaction: {}", refund.getOriginalTransId());
    }

    /**
     * User: Cancel refund request (only if still PENDING)
     */
    @Transactional
    public RefundResponse cancelRefund(Long refundId, Long userId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        // Security check
        if (!refund.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Can only cancel if PENDING or UNDER_REVIEW
        if (refund.getStatus() != Refund.RefundStatus.PENDING &&
                refund.getStatus() != Refund.RefundStatus.UNDER_REVIEW) {
            throw new RuntimeException("Cannot cancel refund with status: " + refund.getStatus());
        }

        refund.setStatus(Refund.RefundStatus.CANCELLED);
        Refund updated = refundRepository.save(refund);

        log.info("Refund {} cancelled by user {}", refundId, userId);

        return mapToResponse(updated);
    }

    /**
     * Notify all admins about new refund request
     */
    private void notifyAdminsAboutNewRefund(Refund refund) {
        List<User> admins = userRepository.findAll().stream()
                .filter(User::getIsAdmin)
                .collect(Collectors.toList());

        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getUserId(),
                    com.example.buddyfinder_backend.entity.Notification.NotificationType.ADMIN,
                    "New Refund Request",
                    "User " + refund.getUser().getName() + " requested refund of $" + refund.getOriginalAmount(),
                    refund.getRefundId(),
                    "REFUND"
            );
        }
    }

    /**
     * Map entity to DTO
     */
    private RefundResponse mapToResponse(Refund refund) {
        return RefundResponse.builder()
                .refundId(refund.getRefundId())
                .userId(refund.getUser().getUserId())
                .userName(refund.getUser().getName())
                .subId(refund.getSubId())
                .refundMethod(refund.getRefundMethod().name())
                .originalAmount(refund.getOriginalAmount())
                .refundType(refund.getRefundType().name())
                .reason(refund.getReason().name())
                .description(refund.getDescription())
                .status(refund.getStatus().name())
                .paymentGateway(refund.getPaymentGateway())
                .originalTransId(refund.getOriginalTransId())
                .refundTransId(refund.getRefundTransId())
                .adminNotes(refund.getAdminNotes())
                .requestedAt(refund.getRequestedAt())
                .processedAt(refund.getProcessedAt())
                .completedAt(refund.getCompletedAt())
                .processedByName(refund.getProcessedBy() != null ? refund.getProcessedBy().getName() : null)
                .build();
    }
}