package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.entity.Profile;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.ProfileRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public Profile getProfileByUserId(Long userId) {
        return profileRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));
    }

    public Profile updateProfile(Long userId, Map<String, Object> updates) {
        Profile profile = getProfileByUserId(userId);

        if (updates.containsKey("photos")) {
            profile.setPhotos((String) updates.get("photos"));
        }
        if (updates.containsKey("fitnessGoals")) {
            profile.setFitnessGoals((String) updates.get("fitnessGoals"));
        }
        if (updates.containsKey("preferredActivities")) {
            profile.setPreferredActivities((String) updates.get("preferredActivities"));
        }
        if (updates.containsKey("workoutFrequency")) {
            profile.setWorkoutFrequency((Integer) updates.get("workoutFrequency"));
        }
        if (updates.containsKey("experienceLevel")) {
            profile.setExperienceLevel((String) updates.get("experienceLevel"));
        }
        if (updates.containsKey("certifications")) {
            profile.setCertifications((String) updates.get("certifications"));
        }
        if (updates.containsKey("gymLocation")) {
            profile.setGymLocation((String) updates.get("gymLocation"));
        }
        if (updates.containsKey("workoutTimePref")) {
            profile.setWorkoutTimePref((String) updates.get("workoutTimePref"));
        }

        return profileRepository.save(profile);
    }

    public Profile uploadPhoto(Long userId, MultipartFile file) {
        Profile profile = getProfileByUserId(userId);

        // Upload to Cloudinary
        String photoUrl = cloudinaryService.uploadImage(file);
        System.out.println("✅ Uploaded to Cloudinary: " + photoUrl); // DEBUG

        // Get current photos
        List<String> photoList = parsePhotos(profile.getPhotos());
        System.out.println("📷 Current photos: " + photoList); // DEBUG

        // Add new photo
        photoList.add(photoUrl);

        // Limit to 6
        if (photoList.size() > 6) {
            String oldestPhoto = photoList.remove(0);
            cloudinaryService.deleteImage(oldestPhoto);
        }

        // Save as JSON
        String photosJson = toJsonArray(photoList);
        profile.setPhotos(photosJson);

        Profile saved = profileRepository.save(profile);
        System.out.println("💾 Saved photos to DB: " + saved.getPhotos()); // DEBUG

        return saved;
    }

    public Profile deletePhoto(Long userId, String photoUrl) {
        Profile profile = getProfileByUserId(userId);

        List<String> photoList = parsePhotos(profile.getPhotos());

        if (photoList.isEmpty()) {
            throw new RuntimeException("No photos to delete");
        }

        if (!photoList.remove(photoUrl)) {
            throw new RuntimeException("Photo not found");
        }

        cloudinaryService.deleteImage(photoUrl);

        profile.setPhotos(photoList.isEmpty() ? null : toJsonArray(photoList));

        return profileRepository.save(profile);
    }

    private Profile createDefaultProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = Profile.builder()
                .user(user)
                .build();

        return profileRepository.save(profile);
    }

    // ✅ Helper: Parse photos from JSON string
    private List<String> parsePhotos(String photosJson) {
        if (photosJson == null || photosJson.isEmpty() || photosJson.equals("null")) {
            return new ArrayList<>();
        }

        String cleaned = photosJson.replaceAll("[\\[\\]\"]", "").trim();
        if (cleaned.isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(cleaned.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // ✅ Helper: Convert list to JSON array string
    private String toJsonArray(List<String> list) {
        if (list.isEmpty()) {
            return "[]";
        }
        return "[\"" + String.join("\",\"", list) + "\"]";
    }
}