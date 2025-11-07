package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.ChatMessage;
import com.example.buddyfinder_backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{matchId}")
    @SendTo("/topic/match/{matchId}")
    public ChatMessage handleChatMessage(
            @DestinationVariable Long matchId,
            Map<String, Object> payload) {

        Long senderId = Long.valueOf(payload.get("senderId").toString());
        String content = (String) payload.get("content");

        ChatMessage message = messageService.sendMessage(matchId, senderId, content);

        return message;
    }
}