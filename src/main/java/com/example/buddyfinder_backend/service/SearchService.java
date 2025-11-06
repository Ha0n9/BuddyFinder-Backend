package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.LikesRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final LikesRepository likesRepository;

    public List<UserResponse> searchBuddies(Long currentUserId, String location, String interests) {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> !user.getUserId().equals(currentUserId)) // Exclude self
                .filter(user -> user.getIsActive()) // Only active users
                .filter(user -> location == null || user.getLocation().toLowerCase().contains(location.toLowerCase()))
                .filter(user -> interests == null || user.getInterests().toLowerCase().contains(interests.toLowerCase()))
                .filter(user -> !hasAlreadyLiked(currentUserId, user.getUserId())) // Not already liked
                .limit(20) // Limit results
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getPotentialMatches(Long currentUserId) {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> !user.getUserId().equals(currentUserId))
                .filter(user -> user.getIsActive())
                .filter(user -> !hasAlreadyLiked(currentUserId, user.getUserId()))
                .limit(10)
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private boolean hasAlreadyLiked(Long fromUserId, Long toUserId) {
        return likesRepository.existsByFromUser_UserIdAndToUser_UserId(fromUserId, toUserId);
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
                .availability(user.getAvailability())
                .bio(user.getBio())
                .tier(user.getTier() != null ? user.getTier().name() : null)
                .zodiacSign(user.getZodiacSign())
                .mbtiType(user.getMbtiType())
                .fitnessLevel(user.getFitnessLevel())
                .isVerified(user.getIsVerified())
                .isAdmin(user.getIsAdmin())
                .build();
    }
}