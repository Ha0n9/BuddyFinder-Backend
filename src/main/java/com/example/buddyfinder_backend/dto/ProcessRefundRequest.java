package com.example.buddyfinder_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessRefundRequest {
    private String action;        // APPROVE, REJECT
    private String adminNotes;
    private String refundTransId; // Transaction ID from payment gateway
}
