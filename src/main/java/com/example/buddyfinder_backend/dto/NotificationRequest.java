package com.example.buddyfinder_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class NotificationRequest {
    private Long userId;
    private String type;
    private String title;
    private String message;
    private Long relatedId;
    private String relatedType;
}
