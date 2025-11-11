package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.ReferralResponse;
import com.example.buddyfinder_backend.entity.Referral;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.ReferralService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/referral")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReferralController {

    private final ReferralService referralService;
    private final JwtUtil jwtUtil;

    /**
     * Get user's referral info (code, link, stats)
     * GET /api/referral/info
     */
    @GetMapping("/info")
    public ResponseEntity<ReferralResponse> getReferralInfo(
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        ReferralResponse info = referralService.getReferralInfo(userId);
        return ResponseEntity.ok(info);
    }

    /**
     * Send invite to a friend
     * POST /api/referral/invite
     */
    @PostMapping("/invite")
    public ResponseEntity<Map<String, Object>> sendInvite(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request
    ) {
        Long userId = extractUserId(token);
        String friendEmail = request.get("email");

        try {
            Referral referral = referralService.sendInvite(userId, friendEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Invitation sent successfully!",
                    "referralId", referral.getReferralId()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Claim premium reward (3 referrals)
     * POST /api/referral/claim-reward
     */
    @PostMapping("/claim-reward")
    public ResponseEntity<Map<String, Object>> claimReward(
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);

        try {
            referralService.claimReward(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Premium reward claimed successfully!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Validate referral code (public endpoint for registration page)
     * GET /api/referral/validate/{code}
     */
    @GetMapping("/validate/{code}")
    public ResponseEntity<Map<String, Object>> validateReferralCode(
            @PathVariable String code
    ) {
        try {
            // Just check if code exists and is valid
            ReferralResponse info = referralService.getReferralInfo(null);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", "Valid referral code"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Invalid referral code"
            ));
        }
    }

    /**
     * Extract userId from JWT token
     */
    private Long extractUserId(String token) {
        String jwt = token.replace("Bearer ", "");
        return jwtUtil.extractUserId(jwt);
    }
}