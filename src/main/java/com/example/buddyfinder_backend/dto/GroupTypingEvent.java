package com.example.buddyfinder_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupTypingEvent {
    private Long roomId;
    private Long senderId;
    private String senderName;
    private Boolean typing;
    private Long timestamp;
}
