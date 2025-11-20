package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.SearchFilters;
import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.entity.Profile;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.LikesRepository;
import com.example.buddyfinder_backend.repository.ProfileRepository;
import com.example.buddyfinder_backend.repository.RatingRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import com.example.buddyfinder_backend.util.PremiumAccessUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final LikesRepository likesRepository;
    private final ProfileRepository profileRepository;
    private final RatingRepository ratingRepository;

    public List<UserResponse> searchBuddies(Long currentUserId, String location, String interests) {
        SearchFilters filters = SearchFilters.builder()
                .location(location)
                .interests(interests)
                .activity(interests)
                .build();
        return searchInternal(currentUserId, filters, 20);
    }

    public List<UserResponse> searchWithFilters(Long currentUserId, SearchFilters filters) {
        return searchInternal(currentUserId, filters, 20);
    }

    public List<UserResponse> getPotentialMatches(Long currentUserId, SearchFilters filters) {
        return searchInternal(currentUserId, filters, 10);
    }

    public List<UserResponse> getPotentialMatches(Long currentUserId) {
        return searchInternal(currentUserId, null, 10);
    }

    private static final double EARTH_RADIUS_KM = 6371.0;

    private List<UserResponse> searchInternal(Long currentUserId, SearchFilters filters, int limit) {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> !user.getUserId().equals(currentUserId))
                .filter(User::getIsActive)
                .filter(user -> !Boolean.TRUE.equals(user.getIsAdmin()))
                .filter(user -> !Boolean.TRUE.equals(user.getIncognitoMode()))
                .filter(user -> !hasAlreadyLiked(currentUserId, user.getUserId()))
                .filter(user -> matchesFilters(user, filters))
                .limit(limit)
                .map(this::mapToUserResponseWithPhotos)
                .collect(Collectors.toList());
    }

    private boolean hasAlreadyLiked(Long fromUserId, Long toUserId) {
        return likesRepository.existsByFromUser_UserIdAndToUser_UserId(fromUserId, toUserId);
    }

    private boolean matchesFilters(User user, SearchFilters filters) {
        if (filters == null) {
            return true;
        }

        return matchesLocation(user, filters)
                && matchesActivity(user, filters)
                && matchesEquals(user.getMbtiType(), filters.getMbtiType())
                && matchesEquals(user.getZodiacSign(), filters.getZodiacSign())
                && matchesEquals(user.getFitnessLevel(), filters.getFitnessLevel())
                && matchesEquals(user.getGender(), filters.getGender())
                && matchesTime(user.getAvailability(), filters.getTime());
    }

    private boolean matchesLocation(User user, SearchFilters filters) {
        if (filters == null) {
            return true;
        }

        if (isRadiusSearch(filters)) {
            if (user.getLatitude() == null || user.getLongitude() == null) {
                return false;
            }
            double distance = calculateDistanceKm(
                    filters.getLatitude(),
                    filters.getLongitude(),
                    user.getLatitude().doubleValue(),
                    user.getLongitude().doubleValue()
            );
            if (distance > filters.getRadiusKm()) {
                return false;
            }
            return matchesContains(user.getLocation(), filters.getLocation());
        }

        return matchesContains(user.getLocation(), filters.getLocation());
    }

    private boolean isRadiusSearch(SearchFilters filters) {
        return filters.getLatitude() != null
                && filters.getLongitude() != null
                && filters.getRadiusKm() != null
                && filters.getRadiusKm() > 0;
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    private boolean matchesActivity(User user, SearchFilters filters) {
        String activityFilter = firstNonBlank(filters.getActivity(), filters.getInterests());
        if (isBlank(activityFilter)) {
            return true;
        }
        return matchesContains(user.getInterests(), activityFilter);
    }

    private boolean matchesContains(String source, String filter) {
        if (isBlank(filter)) {
            return true;
        }
        if (isBlank(source)) {
            return false;
        }
        return source.toLowerCase().contains(filter.toLowerCase());
    }

    private boolean matchesEquals(String source, String filter) {
        if (isBlank(filter)) {
            return true;
        }
        if (isBlank(source)) {
            return false;
        }
        return source.equalsIgnoreCase(filter.trim());
    }

    private boolean matchesTime(String availability, String timeFilter) {
        if (isBlank(timeFilter)) {
            return true;
        }
        if (isBlank(availability)) {
            return false;
        }
        String normalizedAvailability = availability.toLowerCase();
        return switch (timeFilter.toLowerCase()) {
            case "morning" -> containsAny(normalizedAvailability, "morning", "6am", "before noon");
            case "afternoon" -> containsAny(normalizedAvailability, "afternoon", "midday", "after lunch");
            case "evening" -> containsAny(normalizedAvailability, "evening", "night", "pm", "after work");
            case "weekends" -> containsAny(normalizedAvailability, "weekend", "saturday", "sunday");
            default -> normalizedAvailability.contains(timeFilter.toLowerCase());
        };
    }

    private boolean containsAny(String source, String... terms) {
        for (String term : terms) {
            if (source.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Includes photos from profile
    private UserResponse mapToUserResponseWithPhotos(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .gender(user.getGender())
                .interests(user.getInterests())
                .location(user.getLocation())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .availability(user.getAvailability())
                .bio(user.getBio())
                .tier(user.getTier() != null ? user.getTier().name() : null)
                .fitnessLevel(user.getFitnessLevel())
                .isVerified(user.getIsVerified())
                .isAdmin(user.getIsAdmin())
                .isSuperAdmin(user.getIsSuperAdmin())
                .profilePictureUrl(user.getProfilePictureUrl())
                .incognitoMode(user.getIncognitoMode());

        PremiumAccessUtil.applyPremiumTraits(user, builder);

        // Add photos from profile
        Optional<Profile> profile = profileRepository.findByUser_UserId(user.getUserId());
        profile.ifPresent(p -> builder.photos(p.getPhotos()));

        Double avgRating = ratingRepository.getAverageRating(user.getUserId());
        double roundedAvg = avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0;
        long totalRatings = ratingRepository.countByToUser_UserId(user.getUserId());
        builder.averageRating(avgRating != null ? roundedAvg : null);
        builder.totalRatings(totalRatings);

        return builder.build();
    }

    // Keep old method for backward compatibility
    private UserResponse mapToUserResponse(User user) {
        return mapToUserResponseWithPhotos(user);
    }
}
