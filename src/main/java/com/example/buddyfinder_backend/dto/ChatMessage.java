package com.example.buddyfinder_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long messageId;
    private Long matchId;
    private Long senderId;
    private String senderName;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private LocalDateTime timestamp;
    private Boolean isRead;
}