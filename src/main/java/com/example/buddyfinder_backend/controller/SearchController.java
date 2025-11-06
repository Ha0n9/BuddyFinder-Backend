package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final JwtUtil jwtUtil;

    @GetMapping("/buddies")
    public ResponseEntity<List<UserResponse>> searchBuddies(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String interests) {

        Long userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(searchService.searchBuddies(userId, location, interests));
    }

    @GetMapping("/potential")
    public ResponseEntity<List<UserResponse>> getPotentialMatches(
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(searchService.getPotentialMatches(userId));
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}