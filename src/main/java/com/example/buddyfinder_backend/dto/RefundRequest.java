package com.example.buddyfinder_backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    private Long subId;
    private String refundMethod;      // ORIGINAL_PAYMENT, CREDIT, BANK_TRANSFER
    private BigDecimal originalAmount;
    private String refundType;        // FULL, PARTIAL
    private String reason;            // NOT_SATISFIED, TECHNICAL_ISSUES, etc.
    private String description;
    private String originalTransId;
    private String paymentGateway;
}

