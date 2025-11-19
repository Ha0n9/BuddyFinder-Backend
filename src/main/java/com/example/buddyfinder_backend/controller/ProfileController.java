package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.entity.Profile;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<Profile> getProfile(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    @PutMapping
    public ResponseEntity<Profile> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> updates) {
        Long userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(profileService.updateProfile(userId, updates));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Profile> getProfileByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    @PostMapping("/upload-photo")
    public ResponseEntity<Map<String, Object>> uploadPhoto(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file) {

        Long userId = extractUserIdFromToken(authHeader);
        Profile updatedProfile = profileService.uploadPhoto(userId, file);

        Map<String, Object> response = new HashMap<>();
        response.put("profileId", updatedProfile.getProfileId());
        response.put("photos", updatedProfile.getPhotos()); // ✅ Field photos
        response.put("message", "Photo uploaded successfully");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-photo")
    public ResponseEntity<Map<String, Object>> deletePhoto(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {

        Long userId = extractUserIdFromToken(authHeader);
        String photoUrl = request.get("photoUrl");

        Profile updatedProfile = profileService.deletePhoto(userId, photoUrl);

        Map<String, Object> response = new HashMap<>();
        response.put("profileId", updatedProfile.getProfileId());
        response.put("photos", updatedProfile.getPhotos());
        response.put("message", "Photo deleted successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<Map<String, Object>> uploadProfilePicture(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file) {
        Long userId = extractUserIdFromToken(authHeader);
        String url = profileService.uploadProfilePicture(userId, file);

        Map<String, Object> response = new HashMap<>();
        response.put("profilePictureUrl", url);
        response.put("message", "Profile picture updated");

        return ResponseEntity.ok(response);
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
