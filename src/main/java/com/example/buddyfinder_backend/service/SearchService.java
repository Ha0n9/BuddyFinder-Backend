package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.entity.Profile;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.LikesRepository;
import com.example.buddyfinder_backend.repository.ProfileRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
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
    private final ProfileRepository profileRepository; // ✅ ADD THIS

    public List<UserResponse> searchBuddies(Long currentUserId, String location, String interests) {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> !user.getUserId().equals(currentUserId)) // Exclude self
                .filter(user -> user.getIsActive()) // Only active users
                .filter(user -> location == null || user.getLocation().toLowerCase().contains(location.toLowerCase()))
                .filter(user -> interests == null || user.getInterests().toLowerCase().contains(interests.toLowerCase()))
                .filter(user -> !hasAlreadyLiked(currentUserId, user.getUserId())) // Not already liked
                .limit(20) // Limit results
                .map(this::mapToUserResponseWithPhotos) // ✅ CHANGED
                .collect(Collectors.toList());
    }

    public List<UserResponse> getPotentialMatches(Long currentUserId) {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> !user.getUserId().equals(currentUserId))
                .filter(user -> user.getIsActive())
                .filter(user -> !hasAlreadyLiked(currentUserId, user.getUserId()))
                .limit(10)
                .map(this::mapToUserResponseWithPhotos) // ✅ CHANGED
                .collect(Collectors.toList());
    }

    private boolean hasAlreadyLiked(Long fromUserId, Long toUserId) {
        return likesRepository.existsByFromUser_UserIdAndToUser_UserId(fromUserId, toUserId);
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
                .availability(user.getAvailability())
                .bio(user.getBio())
                .tier(user.getTier() != null ? user.getTier().name() : null)
                .zodiacSign(user.getZodiacSign())
                .mbtiType(user.getMbtiType())
                .fitnessLevel(user.getFitnessLevel())
                .isVerified(user.getIsVerified())
                .isAdmin(user.getIsAdmin())
                .profilePictureUrl(user.getProfilePictureUrl());

        // Add photos from profile
        Optional<Profile> profile = profileRepository.findByUser_UserId(user.getUserId());
        profile.ifPresent(p -> builder.photos(p.getPhotos()));

        return builder.build();
    }

    // Keep old method for backward compatibility
    private UserResponse mapToUserResponse(User user) {
        return mapToUserResponseWithPhotos(user);
    }
}
