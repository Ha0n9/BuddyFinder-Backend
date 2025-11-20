package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.VerificationResponse;
import com.example.buddyfinder_backend.entity.AccountVerification;
import com.example.buddyfinder_backend.entity.Notification;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.AccountVerificationRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    private final AccountVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final NotificationService notificationService;

    /**
     * User submits verification document
     */
    @Transactional
    public VerificationResponse submitVerification(Long userId, MultipartFile document, String documentType) {
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user already has approved verification
        if (verificationRepository.existsByUser_UserIdAndStatus(
                userId, AccountVerification.VerificationStatus.APPROVED)) {
            throw new RuntimeException("User is already verified");
        }

        // Check if user has pending verification
        verificationRepository.findTopByUser_UserIdOrderBySubmittedAtDesc(userId)
                .ifPresent(existing -> {
                    if (existing.getStatus() == AccountVerification.VerificationStatus.PENDING) {
                        throw new RuntimeException("You already have a pending verification request");
                    }
                });

        // Upload document to Cloudinary
        String documentUrl = cloudinaryService.uploadImage(document);
        log.info("Document uploaded to Cloudinary: {}", documentUrl);

        // Create verification request
        AccountVerification verification = AccountVerification.builder()
                .user(user)
                .documentType(AccountVerification.DocumentType.valueOf(documentType))
                .documentUrl(documentUrl)
                .status(AccountVerification.VerificationStatus.PENDING)
                .build();

        AccountVerification saved = verificationRepository.save(verification);
        log.info("Verification request submitted: ID={}, User={}", saved.getVerificationId(), userId);

        // Notify user
        notificationService.createNotification(
                userId,
                Notification.NotificationType.SYSTEM,
                "Verification Submitted",
                "Your verification request has been submitted and is under review",
                saved.getVerificationId(),
                "VERIFICATION"
        );

        // Notify all admins
        notifyAdminsAboutNewVerification(saved);

        return mapToResponse(saved);
    }

    /**
     * Get user's verification status
     */
    @Transactional(readOnly = true)
    public VerificationResponse getUserVerificationStatus(Long userId) {
        AccountVerification verification = verificationRepository
                .findTopByUser_UserIdOrderBySubmittedAtDesc(userId)
                .orElse(null);

        if (verification == null) {
            // Return null to indicate no verification exists
            return null;
        }

        return mapToResponse(verification);
    }

    /**
     * Admin: Get all pending verifications
     */
    @Transactional(readOnly = true)
    public List<VerificationResponse> getPendingVerifications(Long adminId) {
        verifyAdmin(adminId);

        List<AccountVerification> verifications = verificationRepository.findPendingVerifications();
        return verifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Admin: Get all verifications
     */
    @Transactional(readOnly = true)
    public List<VerificationResponse> getAllVerifications(Long adminId) {
        verifyAdmin(adminId);

        List<AccountVerification> verifications = verificationRepository.findAll();
        return verifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Admin: Approve verification
     */
    @Transactional
    public VerificationResponse approveVerification(Long verificationId, Long adminId, String adminNotes) {
        User admin = verifyAdmin(adminId);

        AccountVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new RuntimeException("Verification not found"));

        if (verification.getStatus() != AccountVerification.VerificationStatus.PENDING) {
            throw new RuntimeException("Only pending verifications can be approved");
        }

        // Update verification
        verification.setStatus(AccountVerification.VerificationStatus.APPROVED);
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(admin);
        verification.setAdminNotes(adminNotes);

        // Mark user as verified
        User user = verification.getUser();
        user.setIsVerified(true);
        userRepository.save(user);

        AccountVerification updated = verificationRepository.save(verification);
        log.info("Verification {} approved by admin {}", verificationId, adminId);

        // Notify user
        notificationService.createNotification(
                user.getUserId(),
                Notification.NotificationType.SYSTEM,
                "Account Verified!",
                "Congratulations! Your account has been verified. You now have a verified badge on your profile.",
                verificationId,
                "VERIFICATION"
        );

        return mapToResponse(updated);
    }

    /**
     * Admin: Reject verification
     */
    @Transactional
    public VerificationResponse rejectVerification(Long verificationId, Long adminId, String adminNotes) {
        User admin = verifyAdmin(adminId);

        AccountVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new RuntimeException("Verification not found"));

        if (verification.getStatus() != AccountVerification.VerificationStatus.PENDING) {
            throw new RuntimeException("Only pending verifications can be rejected");
        }

        // Update verification
        verification.setStatus(AccountVerification.VerificationStatus.REJECTED);
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(admin);
        verification.setAdminNotes(adminNotes != null ? adminNotes : "Verification request rejected");

        AccountVerification updated = verificationRepository.save(verification);
        log.info("Verification {} rejected by admin {}", verificationId, adminId);

        // Notify user
        notificationService.createNotification(
                verification.getUser().getUserId(),
                Notification.NotificationType.SYSTEM,
                "Verification Rejected",
                "Your verification request has been rejected. " +
                        (adminNotes != null ? "Reason: " + adminNotes : "Please submit a clearer document."),
                verificationId,
                "VERIFICATION"
        );

        return mapToResponse(updated);
    }

    /**
     * Verify admin privileges
     */
    private User verifyAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getIsAdmin()) {
            throw new RuntimeException("Unauthorized: Not an admin");
        }

        return admin;
    }

    /**
     * Notify all admins about new verification request
     */
    private void notifyAdminsAboutNewVerification(AccountVerification verification) {
        List<User> admins = userRepository.findAll().stream()
                .filter(User::getIsAdmin)
                .collect(Collectors.toList());

        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getUserId(),
                    Notification.NotificationType.ADMIN,
                    "New Verification Request",
                    "User " + verification.getUser().getName() + " submitted a verification request",
                    verification.getVerificationId(),
                    "VERIFICATION"
            );
        }
    }

    /**
     * Map entity to DTO
     */
    private VerificationResponse mapToResponse(AccountVerification verification) {
        return VerificationResponse.builder()
                .verificationId(verification.getVerificationId())
                .userId(verification.getUser().getUserId())
                .userName(verification.getUser().getName())
                .userEmail(verification.getUser().getEmail())
                .documentType(verification.getDocumentType().name())
                .documentUrl(verification.getDocumentUrl())
                .status(verification.getStatus().name())
                .adminNotes(verification.getAdminNotes())
                .reviewedByName(verification.getReviewedBy() != null ?
                        verification.getReviewedBy().getName() : null)
                .submittedAt(verification.getSubmittedAt())
                .reviewedAt(verification.getReviewedAt())
                .build();
    }
}