package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.ChatMessage;
import com.example.buddyfinder_backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{matchId}")
    public void handleChatMessage(
            @DestinationVariable Long matchId,
            Map<String, Object> payload) {

        log.info("📨 Received WebSocket message for match {}: {}", matchId, payload);

        try {
            Long senderId = Long.valueOf(payload.get("senderId").toString());
            String content = (String) payload.get("content");

            // Save message to database
            ChatMessage message = messageService.sendMessage(matchId, senderId, content);

            log.info("💾 Message saved: {}", message);

            // ✅ FIX: Broadcast to ALL subscribers of this match
            String destination = "/topic/match/" + matchId;
            messagingTemplate.convertAndSend(destination, message);

            log.info("📡 Broadcasted message to: {}", destination);

        } catch (Exception e) {
            log.error("❌ Error handling chat message: ", e);
        }
    }
}