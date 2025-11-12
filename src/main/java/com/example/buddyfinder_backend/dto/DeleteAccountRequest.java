package com.example.buddyfinder_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Delete Account Request
 * GDPR Compliance - Right to Erasure (Article 17)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountRequest {

    @NotBlank(message = "Password is required for account deletion")
    private String password;
}