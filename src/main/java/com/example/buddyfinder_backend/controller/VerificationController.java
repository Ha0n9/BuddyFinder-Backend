package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.VerificationResponse;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class VerificationController {

    private final VerificationService verificationService;
    private final JwtUtil jwtUtil;

    /**
     * User: Submit verification document
     * POST /api/verification/submit
     */
    @PostMapping("/submit")
    public ResponseEntity<VerificationResponse> submitVerification(
            @RequestHeader("Authorization") String token,
            @RequestParam("document") MultipartFile document,
            @RequestParam("documentType") String documentType
    ) {
        Long userId = extractUserId(token);

        log.info("📝 Verification submission: userId={}, documentType={}", userId, documentType);

        VerificationResponse verification = verificationService.submitVerification(
                userId, document, documentType);

        return ResponseEntity.status(HttpStatus.CREATED).body(verification);
    }

    /**
     * User: Get own verification status
     * GET /api/verification/status
     */
    @GetMapping("/status")
    public ResponseEntity<VerificationResponse> getVerificationStatus(
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        VerificationResponse status = verificationService.getUserVerificationStatus(userId);

        // Return 204 No Content if no verification exists
        if (status == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(status);
    }

    /**
     * Admin: Get all pending verifications
     * GET /api/verification/admin/pending
     */
    @GetMapping("/admin/pending")
    public ResponseEntity<?> getPendingVerifications(
            @RequestHeader("Authorization") String token
    ) {
        if (!isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }

        Long adminId = extractUserId(token);
        List<VerificationResponse> verifications = verificationService.getPendingVerifications(adminId);
        return ResponseEntity.ok(verifications);
    }

    /**
     * Admin: Get all verifications
     * GET /api/verification/admin/all
     */
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllVerifications(
            @RequestHeader("Authorization") String token
    ) {
        if (!isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }

        Long adminId = extractUserId(token);
        List<VerificationResponse> verifications = verificationService.getAllVerifications(adminId);
        return ResponseEntity.ok(verifications);
    }

    /**
     * Admin: Approve verification
     * PUT /api/verification/admin/{id}/approve
     */
    @PutMapping("/admin/{id}/approve")
    public ResponseEntity<?> approveVerification(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @RequestBody(required = false) Map<String, String> body
    ) {
        if (!isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }

        Long adminId = extractUserId(token);
        String adminNotes = body != null ? body.get("adminNotes") : null;

        VerificationResponse verification = verificationService.approveVerification(
                id, adminId, adminNotes);

        return ResponseEntity.ok(verification);
    }

    /**
     * Admin: Reject verification
     * PUT /api/verification/admin/{id}/reject
     */
    @PutMapping("/admin/{id}/reject")
    public ResponseEntity<?> rejectVerification(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @RequestBody(required = false) Map<String, String> body
    ) {
        if (!isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }

        Long adminId = extractUserId(token);
        String adminNotes = body != null ? body.get("adminNotes") : null;

        VerificationResponse verification = verificationService.rejectVerification(
                id, adminId, adminNotes);

        return ResponseEntity.ok(verification);
    }

    /**
     * Extract userId from JWT token
     */
    private Long extractUserId(String token) {
        String jwt = token.replace("Bearer ", "");
        return jwtUtil.extractUserId(jwt);
    }

    /**
     * Check if user is admin
     */
    private boolean isAdmin(String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            return jwtUtil.extractIsAdmin(jwt);
        } catch (Exception e) {
            return false;
        }
    }
}