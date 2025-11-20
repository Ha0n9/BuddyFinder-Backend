package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.DeleteAccountRequest;
import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.UserRepository;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    /**
     * Get current user's profile
     * GET /api/users/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    /**
     * Update current user's profile
     * PUT /api/users/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> updates) {
        Long userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(userService.updateProfile(userId, updates));
    }

    /**
     * Get user by ID (for viewing other profiles)
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Delete account - GDPR Compliant
     * DELETE /api/users/account
     * Permanently deletes user account and all associated data
     *
     * GDPR Article 17 - Right to Erasure ("Right to be Forgotten")
     */
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DeleteAccountRequest request) {
        try {
            // Extract user ID from JWT token
            Long userId = extractUserIdFromToken(authHeader);

            log.info("🗑️ Delete account request from userId: {}", userId);

            // Find user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            log.info("👤 User found: {} ({})", user.getName(), user.getEmail());

            // Verify password for security
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("Invalid password for account deletion attempt - userId: {}", userId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid password"));
            }

            // Delete user account and all related data (GDPR compliant)
            userService.deleteUserAccount(userId);

            log.info("Account successfully deleted - userId: {}", userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Account deleted successfully. All your data has been permanently removed.");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error during account deletion", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete account. Please try again later."));
        }
    }

    /**
     * Extract user ID from JWT token
     */
    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer "
        return jwtUtil.extractUserId(token);
    }
}