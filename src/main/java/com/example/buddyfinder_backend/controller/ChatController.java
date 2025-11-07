// src/main/java/com/example/buddyfinder_backend/controller/ChatController.java
package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.ChatMessage;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final JwtUtil jwtUtil;

    @GetMapping("/messages/{matchId}")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long matchId) {

        Long userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(messageService.getMessagesByMatch(matchId, userId));
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {

        Long userId = extractUserIdFromToken(authHeader);
        Long matchId = Long.valueOf(request.get("matchId").toString());
        String content = (String) request.get("content");

        ChatMessage message = messageService.sendMessage(matchId, userId, content);

        return ResponseEntity.ok(message);
    }

    @GetMapping("/unread/{matchId}")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long matchId) {

        Long userId = extractUserIdFromToken(authHeader);
        Integer count = messageService.getUnreadCount(matchId, userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}