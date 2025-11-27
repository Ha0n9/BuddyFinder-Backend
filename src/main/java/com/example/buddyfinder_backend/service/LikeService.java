package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.entity.Likes;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.LikesRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikesRepository likesRepository;
    private final UserRepository userRepository;

    public List<UserResponse> getUsersWhoLiked(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getTier() != User.TierType.ELITE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Upgrade to Elite to see who liked you");
        }

        return likesRepository.findByToUser_UserId(userId).stream()
                .map(Likes::getFromUser)
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
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
                .zodiacSign(user.getZodiacSign())
                .mbtiType(user.getMbtiType())
                .fitnessLevel(user.getFitnessLevel())
                .isVerified(user.getIsVerified())
                .isAdmin(user.getIsAdmin())
                .isSuperAdmin(user.getIsSuperAdmin())
                .profilePictureUrl(user.getProfilePictureUrl())
                .incognitoMode(user.getIncognitoMode())
                .build();
    }
}
