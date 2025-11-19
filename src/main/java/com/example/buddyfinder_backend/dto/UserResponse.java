package com.example.buddyfinder_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String name;
    private String email;
    private Integer age;
    private String gender;
    private String interests;
    private String location;
    private Boolean availability;
    private String bio;
    private String tier;
    private String zodiacSign;
    private String mbtiType;
    private String fitnessLevel;
    private Boolean isVerified;
    private Boolean isAdmin;
    private String photos;
    private String profilePictureUrl;
}
