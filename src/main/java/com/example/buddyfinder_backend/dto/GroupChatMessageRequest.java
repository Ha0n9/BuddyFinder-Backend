package com.example.buddyfinder_backend.dto;

import lombok.Data;

@Data
public class GroupChatMessageRequest {
    private Long senderId;
    private String content;
}
