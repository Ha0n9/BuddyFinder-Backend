package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.ProcessRefundRequest;
import com.example.buddyfinder_backend.dto.RefundRequest;
import com.example.buddyfinder_backend.dto.RefundResponse;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RefundController {

    private final RefundService refundService;
    private final JwtUtil jwtUtil;

    /**
     * User: Request refund
     * POST /api/refunds
     */
    @PostMapping
    public ResponseEntity<RefundResponse> requestRefund(
            @RequestHeader("Authorization") String token,
            @RequestBody RefundRequest request
    ) {
        Long userId = extractUserId(token);
        RefundResponse refund = refundService.requestRefund(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(refund);
    }

    /**
     * User: Get own refunds
     * GET /api/refunds/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<RefundResponse>> getMyRefunds(
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        List<RefundResponse> refunds = refundService.getUserRefunds(userId);
        return ResponseEntity.ok(refunds);
    }

    /**
     * User: Get refund by ID
     * GET /api/refunds/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RefundResponse> getRefundById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token
    ) {
        RefundResponse refund = refundService.getRefundById(id);

        // Security check: User can only see their own refunds (unless admin)
        Long userId = extractUserId(token);
        if (!refund.getUserId().equals(userId) && !isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(refund);
    }

    /**
     * User: Cancel refund request
     * PUT /api/refunds/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<RefundResponse> cancelRefund(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        RefundResponse refund = refundService.cancelRefund(id, userId);
        return ResponseEntity.ok(refund);
    }

    /**
     * Admin: Get all pending refunds
     * GET /api/refunds/admin/pending
     */
    @GetMapping("/admin/pending")
    public ResponseEntity<?> getPendingRefunds(
            @RequestHeader("Authorization") String token
    ) {
        if (!isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }

        List<RefundResponse> refunds = refundService.getPendingRefunds();
        return ResponseEntity.ok(refunds);
    }

    /**
     * Admin: Process refund (Approve/Reject)
     * PUT /api/refunds/{id}/process
     */
    @PutMapping("/{id}/process")
    public ResponseEntity<?> processRefund(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @RequestBody ProcessRefundRequest request
    ) {
        if (!isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }

        Long adminId = extractUserId(token);
        RefundResponse refund = refundService.processRefund(id, adminId, request);
        return ResponseEntity.ok(refund);
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