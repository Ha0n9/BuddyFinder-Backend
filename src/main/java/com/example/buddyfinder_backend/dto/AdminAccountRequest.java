package com.example.buddyfinder_backend.dto;

import lombok.Data;

@Data
public class AdminAccountRequest {
    private String name;
    private String email;
    private String password;
    private String role; // ADMIN or SUPER_ADMIN
}
