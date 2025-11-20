package com.example.buddyfinder_backend.util;

import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.entity.User;

public final class PremiumAccessUtil {

    private PremiumAccessUtil() {
    }

    public static boolean hasAdvancedTraits(User user) {
        if (user == null || user.getTier() == null) {
            return false;
        }
        return user.getTier() == User.TierType.PREMIUM || user.getTier() == User.TierType.ELITE;
    }

    public static void applyPremiumTraits(User user, UserResponse.UserResponseBuilder builder) {
        if (builder == null) {
            return;
        }

        if (hasAdvancedTraits(user)) {
            builder.zodiacSign(user != null ? user.getZodiacSign() : null);
            builder.mbtiType(user != null ? user.getMbtiType() : null);
        } else {
            builder.zodiacSign(null);
            builder.mbtiType(null);
        }
    }
}
