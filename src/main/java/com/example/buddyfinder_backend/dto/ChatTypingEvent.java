package com.example.buddyfinder_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatTypingEvent {
    private Long matchId;
    private Long senderId;
    private Boolean typing;
    private Long timestamp;
}
