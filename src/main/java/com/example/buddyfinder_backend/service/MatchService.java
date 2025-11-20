package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.entity.Likes;
import com.example.buddyfinder_backend.entity.Match;
import com.example.buddyfinder_backend.entity.Profile;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.LikesRepository;
import com.example.buddyfinder_backend.repository.MatchRepository;
import com.example.buddyfinder_backend.repository.ProfileRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import com.example.buddyfinder_backend.util.PremiumAccessUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final LikesRepository likesRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final NotificationService notificationService;

    public String likeUser(Long fromUserId, Long toUserId) {
        // Check if already liked
        if (likesRepository.existsByFromUser_UserIdAndToUser_UserId(fromUserId, toUserId)) {
            return "Already liked this user";
        }

        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // Create like
        Likes like = Likes.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .type(Likes.LikeType.LIKE)
                .build();

        likesRepository.save(like);

        // Check if mutual like (match)
        Optional<Likes> mutualLike = likesRepository.findByFromUser_UserIdAndToUser_UserId(toUserId, fromUserId);

        if (mutualLike.isPresent()) {
            // Create match
            Match match = createMatch(fromUser, toUser);
            notificationService.notifyMatch(
                    fromUserId,
                    match.getMatchId(),
                    toUser.getName());
            notificationService.notifyMatch(
                    toUserId,
                    match.getMatchId(),
                    fromUser.getName()
            );
            System.out.println("Match created with ID: " + match.getMatchId());
            return "It's a match!";
        }

        return "Like sent!";
    }

    public String passUser(Long fromUserId, Long toUserId) {
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        Likes pass = Likes.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .type(Likes.LikeType.PASS)
                .build();

        likesRepository.save(pass);
        return "Passed";
    }

    public List<UserResponse> getMatches(Long userId) {
        List<Match> matches = matchRepository.findActiveMatchesByUserId(userId);

        List<UserResponse> matchedUsers = new ArrayList<>();

        for (Match match : matches) {
            User matchedUser = match.getUser1().getUserId().equals(userId)
                    ? match.getUser2()
                    : match.getUser1();

            matchedUsers.add(mapToUserResponseWithPhotos(matchedUser)); // CHANGED
        }

        return matchedUsers;
    }

    public List<Map<String, Object>> getMatchesWithDetails(Long userId) {
        List<Match> matches = matchRepository.findActiveMatchesByUserId(userId);

        return matches.stream().map(match -> {
            User matchedUser = match.getUser1().getUserId().equals(userId)
                    ? match.getUser2()
                    : match.getUser1();

            Map<String, Object> matchDetails = new HashMap<>();
            matchDetails.put("matchId", match.getMatchId());
            matchDetails.put("userId", matchedUser.getUserId());
            matchDetails.put("name", matchedUser.getName());
            matchDetails.put("email", matchedUser.getEmail());
            matchDetails.put("age", matchedUser.getAge());
            matchDetails.put("gender", matchedUser.getGender());
            matchDetails.put("interests", matchedUser.getInterests());
            matchDetails.put("location", matchedUser.getLocation());
            matchDetails.put("bio", matchedUser.getBio());
            matchDetails.put("fitnessLevel", matchedUser.getFitnessLevel());
            matchDetails.put("matchedAt", match.getMatchedAt());
            matchDetails.put("profilePictureUrl", matchedUser.getProfilePictureUrl());

            // ADD PHOTOS
            Optional<Profile> profile = profileRepository.findByUser_UserId(matchedUser.getUserId());
            profile.ifPresent(p -> matchDetails.put("photos", p.getPhotos()));

            return matchDetails;
        }).collect(Collectors.toList());
    }

    private Match createMatch(User user1, User user2) {
        // Check if match already exists
        Optional<Match> existingMatch = matchRepository.findMatchBetweenUsers(user1.getUserId(), user2.getUserId());

        if (existingMatch.isPresent()) {
            return existingMatch.get();
        }

        Match match = Match.builder()
                .user1(user1)
                .user2(user2)
                .status(Match.MatchStatus.ACTIVE)
                .compatibilityScore(calculateCompatibility(user1, user2))
                .build();

        return matchRepository.save(match);
    }

    private Float calculateCompatibility(User user1, User user2) {
        // Simple compatibility score based on common interests
        if (user1.getInterests() == null || user2.getInterests() == null) {
            return 50f;
        }

        String[] interests1 = user1.getInterests().toLowerCase().split(",");
        String[] interests2 = user2.getInterests().toLowerCase().split(",");

        int commonInterests = 0;
        for (String interest1 : interests1) {
            for (String interest2 : interests2) {
                if (interest1.trim().equals(interest2.trim())) {
                    commonInterests++;
                }
            }
        }

        float score = (float) commonInterests / Math.max(interests1.length, interests2.length) * 100;
        return Math.min(score, 100f);
    }

    // Includes photos
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

        return builder.build();
    }

    // Keep old method for backward compatibility
    private UserResponse mapToUserResponse(User user) {
        return mapToUserResponseWithPhotos(user);
    }
}
