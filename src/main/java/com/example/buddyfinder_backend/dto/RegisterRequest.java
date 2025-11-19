package com.example.buddyfinder_backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 35, message = "Name must be 35 characters or fewer")
    private String name;

    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Must be at least 18 years old")
    @Max(value = 65, message = "Must be 65 years old or younger")
    private Integer age;

    @NotBlank(message = "Interests are required")
    private String interests;

    @NotBlank(message = "Location is required")
    @Size(max = 40, message = "Location must be 40 characters or fewer")
    private String location;

    @NotBlank(message = "Availability is required")
    private String availability;

    // Referral code
    private String referralCode;
}
