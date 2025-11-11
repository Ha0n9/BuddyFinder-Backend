package com.example.buddyfinder_backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Must be at least 18 years old")
    private Integer age;

    @NotBlank(message = "Interests are required")
    private String interests;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Availability is required")
    private String availability;

    // Referral code
    private String referralCode;
}