package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SubscriptionController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/upgrade")
    public ResponseEntity<UserResponse> upgradeTier(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> payload) {
        String plan = payload.get("plan");
        if (plan == null || plan.isBlank()) {
            throw new IllegalArgumentException("Plan is required");
        }
        Long userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(userService.updateTier(userId, plan));
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
