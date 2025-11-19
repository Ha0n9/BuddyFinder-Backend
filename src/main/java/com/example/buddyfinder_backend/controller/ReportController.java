package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReport(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(reportService.submitReport(userId, payload));
    }

    @GetMapping("/filed")
    public ResponseEntity<List<Map<String, Object>>> reportsFiledByUser(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(reportService.getReportsFiledBy(userId));
    }

    @GetMapping("/against-me")
    public ResponseEntity<List<Map<String, Object>>> reportsAgainstUser(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(reportService.getReportsAgainst(userId));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<Map<String, Object>> getReport(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long reportId) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(reportService.getReportById(reportId, userId));
    }

    @PostMapping("/{reportId}/messages")
    public ResponseEntity<Map<String, Object>> addMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long reportId,
            @RequestBody Map<String, Object> payload) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(reportService.addMessage(reportId, userId, payload));
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
