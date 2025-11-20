package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.GroupChatMessageRequest;
import com.example.buddyfinder_backend.dto.GroupChatMessageResponse;
import com.example.buddyfinder_backend.dto.GroupTypingEvent;
import com.example.buddyfinder_backend.service.GroupChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketGroupChatController {

    private final GroupChatService groupChatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/group/{roomId}")
    public void handleGroupChat(
            @DestinationVariable Long roomId,
            @Payload GroupChatMessageRequest request
    ) {
        GroupChatMessageResponse msg = groupChatService.sendMessage(roomId, request);
        messagingTemplate.convertAndSend("/topic/group/" + roomId, msg);
    }

    @MessageMapping("/group/{roomId}/typing")
    public void handleGroupTyping(
            @DestinationVariable Long roomId,
            @Payload GroupTypingEvent request
    ) {
        try {
            if (request == null || request.getSenderId() == null) {
                return;
            }
            GroupTypingEvent event = groupChatService.buildTypingEvent(
                    roomId,
                    request.getSenderId(),
                    Boolean.TRUE.equals(request.getTyping())
            );
            messagingTemplate.convertAndSend("/topic/group/" + roomId + "/typing", event);
        } catch (Exception e) {
            log.error("Failed to handle group typing event for room {}", roomId, e);
        }
    }
}
