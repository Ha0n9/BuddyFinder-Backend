package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.*;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.UserRepository;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.util.SanitizeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ReferralService referralService;

    public AuthResponse register(RegisterRequest request) {
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        String normalizedName = SanitizeUtil.sanitize(request.getName());
        if (normalizedName.length() > 35) {
            throw new IllegalArgumentException("Name must be 35 characters or fewer");
        }

        String normalizedLocation = SanitizeUtil.sanitize(request.getLocation());
        if (normalizedLocation.length() > 40) {
            throw new IllegalArgumentException("Location must be 40 characters or fewer");
        }

        String sanitizedInterests = SanitizeUtil.sanitize(request.getInterests());
        String sanitizedAvailability = SanitizeUtil.sanitize(request.getAvailability());

        // Create new user
        User user = User.builder()
                .name(normalizedName)
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .age(request.getAge())
                .interests(sanitizedInterests)
                .location(normalizedLocation)
                .availability(sanitizedAvailability)
                .tier(User.TierType.FREE)
                .isActive(true)
                .isVerified(false)
                .isAdmin(false)  // New users are not admin by default
                .build();

        User savedUser = userRepository.save(user);

        // Track referral if code was provided
        referralService.processReferralSignup(request.getReferralCode(), savedUser.getUserId());

        // UPDATED: Generate JWT token with isAdmin flag
        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getUserId(),
                savedUser.getIsAdmin()
        );

        return new AuthResponse(token, mapToUserResponse(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            if (user.getBanUntil() != null && user.getBanUntil().isBefore(java.time.LocalDateTime.now())) {
                user.setIsActive(true);
                user.setBanUntil(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Your account has been banned. Please contact support.");
            }
        }

        // UPDATED: Generate JWT token with isAdmin flag
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getUserId(),
                user.getIsAdmin()
        );

        return new AuthResponse(token, mapToUserResponse(user));
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
                .incognitoMode(user.getIncognitoMode())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }
}
