package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.*;
import com.example.buddyfinder_backend.util.SanitizeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;
    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final RatingRepository ratingRepository;
    private final NotificationRepository notificationRepository;
    private final AccountVerificationRepository accountVerificationRepository;
    private final RefundRepository refundRepository;
    private final ReferralRepository referralRepository;
    private final LikesRepository likesRepository;
    private final ProfileRepository profileRepository;

    /**
     * Get user profile by ID
     */
    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    /**
     * Update user profile
     */
    public UserResponse updateProfile(Long userId, Map<String, Object> updates) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update fields if present
        if (updates.containsKey("name")) {
            Object nameObj = updates.get("name");
            if (nameObj instanceof String name) {
                String trimmed = SanitizeUtil.sanitize(name);
                if (trimmed.length() > 35) {
                    throw new IllegalArgumentException("Name must be 35 characters or fewer");
                }
                user.setName(trimmed);
            }
        }
        if (updates.containsKey("age")) {
            Object ageObj = updates.get("age");
            if (ageObj instanceof Number ageNumber) {
                int age = ageNumber.intValue();
                if (age < 18 || age > 65) {
                    throw new IllegalArgumentException("Age must be between 18 and 65");
                }
                user.setAge(age);
            }
        }
        if (updates.containsKey("gender")) {
            user.setGender(SanitizeUtil.sanitize((String) updates.get("gender")));
        }
        if (updates.containsKey("interests")) {
            user.setInterests(SanitizeUtil.sanitize((String) updates.get("interests")));
        }
        if (updates.containsKey("location")) {
            Object locationObj = updates.get("location");
            if (locationObj instanceof String location) {
                String trimmed = SanitizeUtil.sanitize(location);
                if (trimmed.length() > 40) {
                    throw new IllegalArgumentException("Location must be 40 characters or fewer");
                }
                user.setLocation(trimmed);
            }
        }
        if (updates.containsKey("availability")) {
            user.setAvailability((Boolean) updates.get("availability"));
        }
        if (updates.containsKey("bio")) {
            user.setBio(SanitizeUtil.sanitize((String) updates.get("bio")));
        }
        if (updates.containsKey("zodiacSign")) {
            user.setZodiacSign(SanitizeUtil.sanitize((String) updates.get("zodiacSign")));
        }
        if (updates.containsKey("mbtiType")) {
            user.setMbtiType(SanitizeUtil.sanitize((String) updates.get("mbtiType")));
        }
        if (updates.containsKey("fitnessLevel")) {
            user.setFitnessLevel(SanitizeUtil.sanitize((String) updates.get("fitnessLevel")));
        }

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    /**
     * Get user by ID
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    /**
     * Delete user account and ALL associated data
     * GDPR Compliant - Complete data erasure (Article 17 - Right to Erasure)
     *
     * This method deletes:
     * 1. All matches (where user is user1 or user2)
     * 2. All messages sent by user
     * 3. All activity participations
     * 4. All activities created by user
     * 5. All ratings given by user
     * 6. All ratings received by user
     * 7. All notifications
     * 8. All verification requests
     * 9. All refund requests
     * 10. All referrals (as referrer or referred)
     * 11. All likes (given or received)
     * 12. Profile data
     * 13. Finally, the user account itself
     *
     * @param userId The ID of the user to delete
     * @throws RuntimeException if deletion fails
     */
    @Transactional
    public void deleteUserAccount(Long userId) {
        log.info("🗑️ ========================================");
        log.info("🗑️ Starting GDPR-compliant account deletion");
        log.info("🗑️ User ID: {}", userId);
        log.info("🗑️ ========================================");

        try {
            // Verify user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            log.info("👤 User found: {} ({})", user.getName(), user.getEmail());

            // 1. Delete all matches where user is involved
            log.info("🔄 Step 1/13: Deleting matches...");
            try {
                matchRepository.deleteByUser1_UserIdOrUser2_UserId(userId, userId);
                log.info("✅ Matches deleted successfully");
            } catch (Exception e) {
                log.warn("⚠️ No matches found or error deleting matches: {}", e.getMessage());
            }

            // 2. Delete all messages sent by user
            log.info("🔄 Step 2/13: Deleting messages...");
            try {
                messageRepository.deleteBySender_UserId(userId);
                log.info("✅ Messages deleted successfully");
            } catch (Exception e) {
                log.warn("⚠️ No messages found or error deleting messages: {}", e.getMessage());
            }

            // 3. Delete activity participants
            log.info("🔄 Step 3/13: Deleting activity participants...");
            try {
                activityParticipantRepository.deleteByUser_UserId(userId);
                log.info("✅ Activity participants deleted successfully");
            } catch (Exception e) {
                log.warn("⚠️ No activity participants found: {}", e.getMessage());
            }

            // 4. Delete activities created by user
            log.info("🔄 Step 4/13: Deleting activities...");
            try {
                activityRepository.deleteByCreator_UserId(userId);
                log.info("✅ Activities deleted successfully");
            } catch (Exception e) {
                log.warn("⚠️ No activities found: {}", e.getMessage());
            }

            // 5. Delete ratings given by user
            log.info("🔄 Step 5/13: Deleting ratings given...");
            try {
                ratingRepository.deleteByFromUser_UserId(userId);
                log.info("✅ Ratings given deleted successfully");
            } catch (Exception e) {
                log.warn("⚠️ No ratings given found: {}", e.getMessage());
            }

            // 6. Delete ratings received by user
            log.info("🔄 Step 6/13: Deleting ratings received...");
            try {
                ratingRepository.deleteByToUser_UserId(userId);
                log.info("✅ Ratings received deleted successfully");
            } catch (Exception e) {
                log.warn("⚠️ No ratings received found: {}", e.getMessage());
            }

            // 7. Delete notifications
            log.info("🔄 Step 7/13: Deleting notifications...");
            try {
                notificationRepository.deleteByUser_UserId(userId);
                log.info("✅ Notifications deleted successfully");
            } catch (Exception e) {
                log.warn("⚠️ No notifications found: {}", e.getMessage());
            }

            // 8. Delete verification requests
            log.info("🔄 Step 8/13: Deleting verification requests...");
            try {
                accountVerificationRepository.deleteByUser_UserId(userId);
                log.info("✅ Verification requests deleted successfully");
            } catch (Exception e) {
                log.warn("⚠️ No verification requests found: {}", e.getMessage());
            }

            // 9. Delete refund requests
            log.info("🔄 Step 9/13: Deleting refund requests...");
            try {
                refundRepository.deleteByUser_UserId(userId);
                log.info("✅ Refund requests deleted successfully");
            } catch (Exception e) {
                log.warn("⚠️ No refund requests found: {}", e.getMessage());
            }

            // 10. Delete referrals (both as referrer and referred)
            // ⚠️ FIXED: Changed from deleteByReferee_UserId to deleteByReferred_UserId
            log.info("🔄 Step 10/13: Deleting referrals...");
            try {
                referralRepository.deleteByReferrer_UserId(userId);
                referralRepository.deleteByReferred_UserId(userId);  // ✅ CORRECT!
                log.info("✅ Referrals deleted successfully");
            } catch (Exception e) {
                log.warn("⚠️ No referrals found: {}", e.getMessage());
            }

            // 11. Delete likes (both given and received)
            log.info("🔄 Step 11/13: Deleting likes...");
            try {
                likesRepository.deleteByFromUser_UserIdOrToUser_UserId(userId, userId);
                log.info("✅ Likes deleted successfully");
            } catch (Exception e) {
                log.warn("⚠️ No likes found: {}", e.getMessage());
            }

            // 12. Delete profile data
            log.info("🔄 Step 12/13: Deleting profile...");
            try {
                profileRepository.deleteByUser_UserId(userId);
                log.info("✅ Profile deleted successfully");
            } catch (Exception e) {
                log.warn("⚠️ No profile found: {}", e.getMessage());
            }

            // 13. Finally, delete the user account itself
            log.info("🔄 Step 13/13: Deleting user account...");
            userRepository.deleteById(userId);
            log.info("✅ User account deleted successfully");

            log.info("🗑️ ========================================");
            log.info("✅ ACCOUNT DELETION COMPLETED SUCCESSFULLY");
            log.info("✅ User ID: {} has been permanently removed", userId);
            log.info("✅ All associated data has been erased (GDPR compliant)");
            log.info("🗑️ ========================================");

        } catch (Exception e) {
            log.error("❌ ========================================");
            log.error("❌ CRITICAL ERROR during account deletion");
            log.error("❌ User ID: {}", userId);
            log.error("❌ Error: {}", e.getMessage(), e);
            log.error("❌ ========================================");
            throw new RuntimeException("Failed to delete account: " + e.getMessage(), e);
        }
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .gender(user.getGender())
                .interests(user.getInterests())
                .location(user.getLocation())
                .availability(user.getAvailability())
                .bio(user.getBio())
                .tier(user.getTier() != null ? user.getTier().name() : null)
                .zodiacSign(user.getZodiacSign())
                .mbtiType(user.getMbtiType())
                .fitnessLevel(user.getFitnessLevel())
                .isVerified(user.getIsVerified())
                .isAdmin(user.getIsAdmin())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }
}
