package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.entity.Match;
import com.example.buddyfinder_backend.repository.MatchRepository;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final MatchRepository matchRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/like")
    public ResponseEntity<Map<String, Object>> likeUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Long> request) {

        Long fromUserId = extractUserIdFromToken(authHeader);
        Long toUserId = request.get("toUserId");

        String result = matchService.likeUser(fromUserId, toUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", result);

        // If it's a match, include match details
        if (result.contains("match")) {
            Optional<Match> match = matchRepository.findMatchBetweenUsers(fromUserId, toUserId);
            match.ifPresent(m -> {
                response.put("matchId", m.getMatchId());
                response.put("isMatch", true);
            });
        } else {
            response.put("isMatch", false);
        }

        return ResponseEntity.ok(response);
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
    public ResponseEntity<List<Map<String, Object>>> getMatches(
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(matchService.getMatchesWithDetails(userId));
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}