package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.entity.Activity;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestHeader("Authorization") String authHeader) {

        // Can add admin verification here
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<User> banUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId) {

        Long adminId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(adminService.banUser(userId, adminId));
    }

    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<User> unbanUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId) {

        Long adminId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(adminService.unbanUser(userId, adminId));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId) {

        Long adminId = extractUserIdFromToken(authHeader);
        adminService.deleteUser(userId, adminId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @GetMapping("/activities")
    public ResponseEntity<List<Activity>> getAllActivities(
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(adminService.getAllActivities());
    }

    @DeleteMapping("/activities/{activityId}")
    public ResponseEntity<Map<String, String>> deleteActivity(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long activityId) {

        Long adminId = extractUserIdFromToken(authHeader);
        adminService.deleteActivity(activityId, adminId);
        return ResponseEntity.ok(Map.of("message", "Activity deleted successfully"));
    }

    // ============ REFUND MANAGEMENT ============

    @GetMapping("/refunds")
    public ResponseEntity<List<Map<String, Object>>> getAllRefunds(
            @RequestHeader("Authorization") String authHeader) {

        Long adminId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(adminService.getAllRefunds(adminId));
    }

    @GetMapping("/refunds/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingRefunds(
            @RequestHeader("Authorization") String authHeader) {

        Long adminId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(adminService.getPendingRefunds(adminId));
    }

    @PutMapping("/refunds/{refundId}/approve")
    public ResponseEntity<Map<String, String>> approveRefund(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long refundId,
            @RequestBody(required = false) Map<String, String> notes) {

        Long adminId = extractUserIdFromToken(authHeader);
        String adminNotes = notes != null ? notes.get("adminNotes") : null;
        adminService.approveRefund(refundId, adminId, adminNotes);
        return ResponseEntity.ok(Map.of("message", "Refund approved successfully"));
    }

    @PutMapping("/refunds/{refundId}/reject")
    public ResponseEntity<Map<String, String>> rejectRefund(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long refundId,
            @RequestBody(required = false) Map<String, String> notes) {

        Long adminId = extractUserIdFromToken(authHeader);
        String adminNotes = notes != null ? notes.get("adminNotes") : null;
        adminService.rejectRefund(refundId, adminId, adminNotes);
        return ResponseEntity.ok(Map.of("message", "Refund rejected successfully"));
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}