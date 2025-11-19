package com.example.buddyfinder_backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRoomInfoDto {
    private Long roomId;
    private Long activityId;
    private String activityTitle;
}
