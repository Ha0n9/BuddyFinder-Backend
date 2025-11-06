package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.entity.Rating;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.RatingRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    public Rating submitRating(Long fromUserId, Long toUserId, Map<String, Object> ratingData) {
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // Check if already rated
        Optional<Rating> existingRating = ratingRepository.findByFromUser_UserIdAndToUser_UserId(fromUserId, toUserId);

        if (existingRating.isPresent()) {
            // Update existing rating
            Rating rating = existingRating.get();
            updateRatingFields(rating, ratingData);
            return ratingRepository.save(rating);
        } else {
            // Create new rating
            Rating rating = Rating.builder()
                    .fromUser(fromUser)
                    .toUser(toUser)
                    .rating((Integer) ratingData.get("rating"))
                    .review((String) ratingData.get("review"))
                    .reliabilityScore(ratingData.get("reliabilityScore") != null ?
                            ((Number) ratingData.get("reliabilityScore")).floatValue() : null)
                    .punctualityScore(ratingData.get("punctualityScore") != null ?
                            ((Number) ratingData.get("punctualityScore")).floatValue() : null)
                    .friendlinessScore(ratingData.get("friendlinessScore") != null ?
                            ((Number) ratingData.get("friendlinessScore")).floatValue() : null)
                    .build();

            return ratingRepository.save(rating);
        }
    }

    public List<Rating> getRatingsForUser(Long userId) {
        return ratingRepository.findByToUser_UserId(userId);
    }

    public Double getAverageRating(Long userId) {
        Double avg = ratingRepository.getAverageRating(userId);
        return avg != null ? avg : 0.0;
    }

    public Map<String, Object> getUserRatingStats(Long userId) {
        List<Rating> ratings = ratingRepository.findByToUser_UserId(userId);

        if (ratings.isEmpty()) {
            return Map.of(
                    "averageRating", 0.0,
                    "totalRatings", 0,
                    "averageReliability", 0.0,
                    "averagePunctuality", 0.0,
                    "averageFriendliness", 0.0
            );
        }

        double avgRating = ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);

        double avgReliability = ratings.stream()
                .filter(r -> r.getReliabilityScore() != null)
                .mapToDouble(Rating::getReliabilityScore)
                .average()
                .orElse(0.0);

        double avgPunctuality = ratings.stream()
                .filter(r -> r.getPunctualityScore() != null)
                .mapToDouble(Rating::getPunctualityScore)
                .average()
                .orElse(0.0);

        double avgFriendliness = ratings.stream()
                .filter(r -> r.getFriendlinessScore() != null)
                .mapToDouble(Rating::getFriendlinessScore)
                .average()
                .orElse(0.0);

        return Map.of(
                "averageRating", Math.round(avgRating * 10.0) / 10.0,
                "totalRatings", ratings.size(),
                "averageReliability", Math.round(avgReliability * 10.0) / 10.0,
                "averagePunctuality", Math.round(avgPunctuality * 10.0) / 10.0,
                "averageFriendliness", Math.round(avgFriendliness * 10.0) / 10.0
        );
    }

    private void updateRatingFields(Rating rating, Map<String, Object> updates) {
        if (updates.containsKey("rating")) {
            rating.setRating((Integer) updates.get("rating"));
        }
        if (updates.containsKey("review")) {
            rating.setReview((String) updates.get("review"));
        }
        if (updates.containsKey("reliabilityScore")) {
            rating.setReliabilityScore(((Number) updates.get("reliabilityScore")).floatValue());
        }
        if (updates.containsKey("punctualityScore")) {
            rating.setPunctualityScore(((Number) updates.get("punctualityScore")).floatValue());
        }
        if (updates.containsKey("friendlinessScore")) {
            rating.setFriendlinessScore(((Number) updates.get("friendlinessScore")).floatValue());
        }
    }
}