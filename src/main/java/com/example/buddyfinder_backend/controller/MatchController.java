package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final JwtUtil jwtUtil;

    @PostMapping("/like")
    public ResponseEntity<Map<String, String>> likeUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Long> request) {

        Long fromUserId = extractUserIdFromToken(authHeader);
        Long toUserId = request.get("toUserId");

        String result = matchService.likeUser(fromUserId, toUserId);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/pass")
    public ResponseEntity<Map<String, String>> passUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Long> request) {

        Long fromUserId = extractUserIdFromToken(authHeader);
        Long toUserId = request.get("toUserId");

        String result = matchService.passUser(fromUserId, toUserId);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getMatches(
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(matchService.getMatches(userId));
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}