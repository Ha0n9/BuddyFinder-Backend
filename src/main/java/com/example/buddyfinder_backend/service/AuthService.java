package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.*;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.UserRepository;
import com.example.buddyfinder_backend.security.JwtUtil;
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

    public AuthResponse register(RegisterRequest request) {
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .age(request.getAge())
                .interests(request.getInterests())
                .location(request.getLocation())
                .availability(true)
                .tier(User.TierType.FREE)
                .isActive(true)
                .isVerified(false)
                .isAdmin(false)  // New users are not admin by default
                .build();

        User savedUser = userRepository.save(user);

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
                .build();
    }
}