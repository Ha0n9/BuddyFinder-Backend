package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.entity.Rating;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Rating> submitRating(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {

        Long fromUserId = extractUserIdFromToken(authHeader);
        Long toUserId = Long.valueOf(request.get("toUserId").toString());

        return ResponseEntity.ok(ratingService.submitRating(fromUserId, toUserId, request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Rating>> getRatingsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ratingService.getRatingsForUser(userId));
    }

    @GetMapping("/average/{userId}")
    public ResponseEntity<Map<String, Double>> getAverageRating(@PathVariable Long userId) {
        Double average = ratingService.getAverageRating(userId);
        return ResponseEntity.ok(Map.of("averageRating", average));
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getUserRatingStats(@PathVariable Long userId) {
        return ResponseEntity.ok(ratingService.getUserRatingStats(userId));
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}