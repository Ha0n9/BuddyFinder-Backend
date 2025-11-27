package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final JwtUtil jwtUtil;

    @GetMapping("/received")
    public ResponseEntity<List<UserResponse>> getReceivedLikes(
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(likeService.getUsersWhoLiked(userId));
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
