package com.example.buddyfinder_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private Long refundId;
    private Long userId;
    private String userName;
    private Long subId;
    private String refundMethod;
    private BigDecimal originalAmount;
    private String refundType;
    private String reason;
    private String description;
    private String status;
    private String paymentGateway;
    private String originalTransId;
    private String refundTransId;
    private String adminNotes;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private String processedByName;
}
