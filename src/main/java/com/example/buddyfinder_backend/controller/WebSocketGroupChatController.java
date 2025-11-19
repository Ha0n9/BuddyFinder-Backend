package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.GroupChatMessageRequest;
import com.example.buddyfinder_backend.dto.GroupChatMessageResponse;
import com.example.buddyfinder_backend.service.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
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
}
