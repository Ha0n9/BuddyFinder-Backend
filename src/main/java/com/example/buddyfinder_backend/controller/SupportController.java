package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.entity.SupportRequest;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.SupportRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SupportController {

    private final SupportRequestService supportRequestService;
    private final JwtUtil jwtUtil;

    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> submitContact(@RequestBody Map<String, String> payload) {
        String email = payload.getOrDefault("email", "").trim();
        String message = payload.getOrDefault("message", "").trim();
        SupportRequest saved = supportRequestService.createRequest(email, message);
        return ResponseEntity.ok(Map.of(
                "message", "Support request submitted",
                "requestId", saved.getRequestId()
        ));
    }

    @GetMapping
    public ResponseEntity<List<SupportRequest>> listRequests(@RequestHeader("Authorization") String authHeader) {
        ensureAdmin(authHeader);
        return ResponseEntity.ok(supportRequestService.getAllRequests());
    }

    @PatchMapping("/{requestId}/status")
    public ResponseEntity<SupportRequest> updateStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long requestId,
            @RequestBody Map<String, String> payload) {
        ensureAdmin(authHeader);
        String statusVal = payload.getOrDefault("status", "OPEN");
        SupportRequest.Status status = SupportRequest.Status.valueOf(statusVal.toUpperCase());
        String notes = payload.get("adminNotes");
        return ResponseEntity.ok(supportRequestService.updateStatus(requestId, status, notes));
    }

    private void ensureAdmin(String authHeader) {
        String token = authHeader.substring(7);
        Boolean isAdmin = jwtUtil.extractIsAdmin(token);
        if (isAdmin == null || !isAdmin) {
            throw new RuntimeException("Admin privileges required");
        }
    }
}
