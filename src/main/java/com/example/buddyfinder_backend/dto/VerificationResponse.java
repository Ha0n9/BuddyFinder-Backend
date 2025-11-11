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
public class VerificationResponse {
    private Long verificationId;
    private Long userId;
    private String userName;
    private String userEmail;
    private String documentType;
    private String documentUrl;
    private String status;
    private String adminNotes;
    private String reviewedByName;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
}