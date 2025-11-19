package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.ReferralResponse;
import com.example.buddyfinder_backend.entity.Notification;
import com.example.buddyfinder_backend.entity.Referral;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.ReferralRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralService {

    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Get or create referral code for user
     */
    @Transactional
    public ReferralResponse getReferralInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find existing referrals
        List<Referral> referrals = referralRepository.findByReferrer_UserIdOrderByCreatedAtDesc(userId);

        Referral baseReferral = referrals.stream()
                .filter(this::isPlaceholderReferral)
                .findFirst()
                .orElse(null);

        if (baseReferral == null) {
            baseReferral = createBaseReferral(user);
            referrals.add(0, baseReferral);
        }

        String referralCode = baseReferral.getReferralCode();

        // Count accepted referrals (real ones only)
        Long acceptedCount = referralRepository.countAcceptedReferralsByReferrer(userId);

        // Check if user can claim reward
        Boolean canClaimReward = acceptedCount >= 3;
        boolean featureLocked = acceptedCount >= 3;

        // Check if reward already claimed
        Boolean rewardClaimed = referrals.stream()
                .anyMatch(Referral::getRewardClaimed);

        // Build response
        List<Referral> actualInvites = referrals.stream()
                .filter(ref -> !isPlaceholderReferral(ref))
                .collect(Collectors.toList());

        List<ReferralResponse.ReferralDetail> inviteDetails = actualInvites.stream()
                .map(this::mapToReferralDetail)
                .collect(Collectors.toList());

        long totalInvited = actualInvites.size();

        return ReferralResponse.builder()
                .referralCode(referralCode)
                .referralLink("http://localhost:5173/register?ref=" + referralCode)
                .totalInvited(totalInvited)
                .acceptedCount(acceptedCount)
                .canClaimReward(canClaimReward && !rewardClaimed)
                .rewardClaimed(rewardClaimed)
                .featureLocked(featureLocked)
                .invites(inviteDetails)
                .build();
    }

    /**
     * Send invite to friend
     */
    @Transactional
    public Referral sendInvite(Long referrerId, String friendEmail) {
        User referrer = userRepository.findById(referrerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long acceptedCount = referralRepository.countAcceptedReferralsByReferrer(referrerId);
        if (acceptedCount != null && acceptedCount >= 3) {
            throw new RuntimeException("You've already invited 3 friends. This perk can only be used once.");
        }

        // Validate email
        if (friendEmail == null || friendEmail.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        // Check if user trying to invite themselves
        if (friendEmail.equalsIgnoreCase(referrer.getEmail())) {
            throw new RuntimeException("You cannot invite yourself");
        }

        // Check if email already registered
        if (userRepository.existsByEmail(friendEmail)) {
            throw new RuntimeException("This user is already registered");
        }

        // Check if already invited by this user
        if (referralRepository.existsByReferrer_UserIdAndReferredEmail(referrerId, friendEmail)) {
            throw new RuntimeException("You have already invited this email");
        }

        String referralCode = ensureBaseReferral(referrer).getReferralCode();

        // Create referral
        Referral referral = Referral.builder()
                .referrer(referrer)
                .referralCode(referralCode)
                .referredEmail(friendEmail)
                .status(Referral.ReferralStatus.PENDING)
                .rewardClaimed(false)
                .build();

        Referral saved = referralRepository.save(referral);
        log.info("📧 Invite sent: {} invited {}", referrer.getEmail(), friendEmail);

        // TODO: Send email invitation to friendEmail
        // sendInvitationEmail(friendEmail, referralCode, referrer.getName());

        return saved;
    }

    /**
     * Process referral when new user signs up with code
     */
    @Transactional
    public void processReferralSignup(String referralCode, Long newUserId) {
        if (referralCode == null || referralCode.trim().isEmpty()) {
            return; // No referral code provided
        }

        String trimmedCode = referralCode.trim();
        List<Referral> referralsByCode = referralRepository.findAllByReferralCode(trimmedCode);

        if (referralsByCode.isEmpty()) {
            log.warn("⚠️ Referral signup attempted with invalid code: {}", referralCode);
            return; // Do not block registration if code is invalid
        }

        User newUser = userRepository.findById(newUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Try to find invite matching this email
        Referral emailMatch = referralsByCode.stream()
                .filter(r -> r.getReferredEmail() != null &&
                        r.getReferredEmail().equalsIgnoreCase(newUser.getEmail()))
                .findFirst()
                .orElse(null);

        Referral referrerHolder;

        if (emailMatch != null) {
            referrerHolder = emailMatch;
            emailMatch.setReferred(newUser);
            emailMatch.setStatus(Referral.ReferralStatus.ACCEPTED);
            emailMatch.setAcceptedAt(LocalDateTime.now());
            referralRepository.save(emailMatch);
        } else {
            referrerHolder = referralsByCode.stream()
                    .filter(this::isPlaceholderReferral)
                    .findFirst()
                    .orElse(referralsByCode.get(0));

            Referral newReferral = Referral.builder()
                    .referrer(referrerHolder.getReferrer())
                    .referred(newUser)
                    .referralCode(trimmedCode)
                    .referredEmail(newUser.getEmail())
                    .status(Referral.ReferralStatus.ACCEPTED)
                    .acceptedAt(LocalDateTime.now())
                    .rewardClaimed(false)
                    .build();
            referralRepository.save(newReferral);
        }

        log.info("✅ Referral accepted: {} signed up using code {}", newUser.getEmail(), trimmedCode);

        // Notify referrer
        notificationService.createNotification(
                referrerHolder.getReferrer().getUserId(),
                Notification.NotificationType.SYSTEM,
                "Friend Joined! 🎉",
                newUser.getName() + " just signed up using your referral link!",
                newUserId,
                "REFERRAL"
        );

        // Check if referrer can claim reward
        Long acceptedCount = referralRepository.countAcceptedReferralsByReferrer(
                referrerHolder.getReferrer().getUserId());

        Boolean hasUnclaimedReward = referralRepository.hasUnclaimedReward(
                referrerHolder.getReferrer().getUserId());

        if (Boolean.TRUE.equals(hasUnclaimedReward)) {
            notificationService.createNotification(
                    referrerHolder.getReferrer().getUserId(),
                    Notification.NotificationType.SYSTEM,
                    "Claim Your Reward! 🎁",
                    "You've referred 3 friends! Claim your FREE month of Premium now!",
                    referrerHolder.getReferrer().getUserId(),
                    "REWARD"
            );
        }
    }

    /**
     * Claim premium reward (3 referrals = 1 month premium)
     */
    @Transactional
    public void claimReward(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user has 3+ accepted referrals
        Long acceptedCount = referralRepository.countAcceptedReferralsByReferrer(userId);
        if (acceptedCount < 3) {
            throw new RuntimeException("You need 3 accepted referrals to claim reward");
        }

        // Check if already claimed
        if (referralRepository.hasUnclaimedReward(userId) == Boolean.FALSE) {
            throw new RuntimeException("Reward already claimed");
        }

        // Upgrade user to Premium
        if (user.getTier() == User.TierType.FREE) {
            user.setTier(User.TierType.PREMIUM);
        }
        // TODO: Set premium expiry date (1 month from now)

        userRepository.save(user);

        // Mark referrals as claimed
        List<Referral> referrals = referralRepository.findByReferrer_UserIdOrderByCreatedAtDesc(userId);
        referrals.stream()
                .filter(r -> r.getStatus() == Referral.ReferralStatus.ACCEPTED && !r.getRewardClaimed())
                .limit(3)
                .forEach(r -> {
                    r.setRewardClaimed(true);
                    r.setRewardClaimedAt(LocalDateTime.now());
                    referralRepository.save(r);
                });

        log.info("🎁 Reward claimed: User {} upgraded to Premium", userId);

        // Notify user
        notificationService.createNotification(
                userId,
                Notification.NotificationType.SYSTEM,
                "Premium Activated! 🎉",
                "Your FREE month of Premium has been activated! Enjoy all premium features.",
                userId,
                "REWARD"
        );
    }

    /**
     * Generate unique referral code
     */
    private String generateUniqueReferralCode(User user) {
        String code;
        int attempts = 0;
        do {
            code = generateCode(user);
            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("Failed to generate unique referral code");
            }
        } while (referralRepository.existsByReferralCode(code));

        return code;
    }

    /**
     * Generate code format: USERNAME + 3 random digits
     */
    private String generateCode(User user) {
        String username = user.getName().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (username.length() > 8) {
            username = username.substring(0, 8);
        }
        String random = String.format("%03d", new Random().nextInt(1000));
        return username + random;
    }

    /**
     * Map entity to DTO
     */
    private ReferralResponse.ReferralDetail mapToReferralDetail(Referral referral) {
        return ReferralResponse.ReferralDetail.builder()
                .referralId(referral.getReferralId())
                .referredEmail(referral.getReferredEmail())
                .referredName(referral.getReferred() != null ? referral.getReferred().getName() : null)
                .status(referral.getStatus().name())
                .createdAt(referral.getCreatedAt())
                .acceptedAt(referral.getAcceptedAt())
                .build();
    }

    private Referral createBaseReferral(User user) {
        return referralRepository.save(
                Referral.builder()
                        .referrer(user)
                        .referralCode(generateUniqueReferralCode(user))
                        .status(Referral.ReferralStatus.PENDING)
                        .rewardClaimed(false)
                        .build()
        );
    }

    private Referral ensureBaseReferral(User user) {
        return referralRepository
                .findFirstByReferrer_UserIdAndReferredIsNullAndReferredEmailIsNull(user.getUserId())
                .orElseGet(() -> createBaseReferral(user));
    }

    private boolean isPlaceholderReferral(Referral referral) {
        return referral.getReferred() == null && referral.getReferredEmail() == null;
    }
}
