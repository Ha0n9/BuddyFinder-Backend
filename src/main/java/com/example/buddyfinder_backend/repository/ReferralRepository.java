package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {

    // Find referral by code
    Optional<Referral> findByReferralCode(String referralCode);

    // Find all referrals made by a user
    List<Referral> findByReferrer_UserIdOrderByCreatedAtDesc(Long referrerId);

    // Find referral by referred user
    Optional<Referral> findByReferred_UserId(Long referredUserId);

    // Count accepted referrals for a user
    @Query("SELECT COUNT(r) FROM Referral r WHERE r.referrer.userId = :referrerId AND r.status = 'ACCEPTED'")
    Long countAcceptedReferralsByReferrer(Long referrerId);

    // Check if user has unclaimed reward (3+ accepted referrals)
    @Query("SELECT CASE WHEN COUNT(r) >= 3 THEN true ELSE false END FROM Referral r " +
            "WHERE r.referrer.userId = :referrerId AND r.status = 'ACCEPTED' AND r.rewardClaimed = false")
    Boolean hasUnclaimedReward(Long referrerId);

    // Check if referral code exists
    boolean existsByReferralCode(String referralCode);

    // Check if email already invited by this referrer
    boolean existsByReferrer_UserIdAndReferredEmail(Long referrerId, String referredEmail);

    // === 🆕 DELETE METHODS FOR GDPR COMPLIANCE ===
    // ⚠️ FIXED: Changed from 'referee' to 'referred' to match entity field name
    void deleteByReferrer_UserId(Long referrerId);
    void deleteByReferred_UserId(Long referredId);  // ✅ CORRECT - matches entity field 'referred'
}