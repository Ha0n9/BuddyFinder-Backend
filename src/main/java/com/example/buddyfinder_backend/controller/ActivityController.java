package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.entity.Activity;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Activity> createActivity(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Activity activity) {
        Long userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(activityService.createActivity(activity, userId));
    }

    @GetMapping
    public ResponseEntity<List<Activity>> getAllActivities() {
        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<Activity> getActivityById(@PathVariable Long activityId) {
        return ResponseEntity.ok(activityService.getActivityById(activityId));
    }

    @PostMapping("/{activityId}/join")
    public ResponseEntity<Map<String, String>> joinActivity(
            @PathVariable Long activityId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromToken(authHeader);
        activityService.joinActivity(activityId, userId);
        return ResponseEntity.ok(Map.of("message", "Joined activity successfully"));
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Map<String, String>> deleteActivity(
            @PathVariable Long activityId,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserIdFromToken(authHeader);
        activityService.deleteActivity(activityId, userId);

        return ResponseEntity.ok(Map.of("message", "Activity deleted successfully"));
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}