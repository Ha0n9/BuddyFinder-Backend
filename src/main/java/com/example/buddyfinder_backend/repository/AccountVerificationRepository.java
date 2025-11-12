package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.AccountVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountVerificationRepository extends JpaRepository<AccountVerification, Long> {

    // Find verification by user
    Optional<AccountVerification> findTopByUser_UserIdOrderBySubmittedAtDesc(Long userId);

    // Find all verifications by user
    List<AccountVerification> findByUser_UserIdOrderBySubmittedAtDesc(Long userId);

    // Find pending verifications (for admin)
    List<AccountVerification> findByStatusOrderBySubmittedAtAsc(AccountVerification.VerificationStatus status);

    // Find all pending verifications
    @Query("SELECT v FROM AccountVerification v WHERE v.status = 'PENDING' ORDER BY v.submittedAt ASC")
    List<AccountVerification> findPendingVerifications();

    // Check if user has approved verification
    boolean existsByUser_UserIdAndStatus(Long userId, AccountVerification.VerificationStatus status);

    // Count pending verifications
    long countByStatus(AccountVerification.VerificationStatus status);

    // === 🆕 DELETE METHOD FOR GDPR COMPLIANCE ===
    void deleteByUser_UserId(Long userId);
}