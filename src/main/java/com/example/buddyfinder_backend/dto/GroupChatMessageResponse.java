package com.example.buddyfinder_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GroupChatMessageResponse {

    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private boolean systemMessage;
    private LocalDateTime timestamp;
}
