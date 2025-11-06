package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.entity.Activity;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.ActivityRepository;
import com.example.buddyfinder_backend.repository.MatchRepository;
import com.example.buddyfinder_backend.repository.MessageRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;

    public Map<String, Object> getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .count();
        long totalActivities = activityRepository.count();
        long totalMatches = matchRepository.count();
        long totalMessages = messageRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("totalActivities", totalActivities);
        stats.put("totalMatches", totalMatches);
        stats.put("totalMessages", totalMessages);

        return stats;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User banUser(Long userId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getIsAdmin()) {
            throw new RuntimeException("Unauthorized: Not an admin");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(false);
        return userRepository.save(user);
    }

    public User unbanUser(Long userId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getIsAdmin()) {
            throw new RuntimeException("Unauthorized: Not an admin");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(true);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getIsAdmin()) {
            throw new RuntimeException("Unauthorized: Not an admin");
        }

        userRepository.deleteById(userId);
    }

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public void deleteActivity(Long activityId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getIsAdmin()) {
            throw new RuntimeException("Unauthorized: Not an admin");
        }

        activityRepository.deleteById(activityId);
    }
}