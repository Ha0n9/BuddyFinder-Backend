package com.example.buddyfinder_backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SearchFilters {
    private String location;
    private String interests;
    private String activity;
    private String time;
    private String mbtiType;
    private String zodiacSign;
    private String fitnessLevel;
    private String gender;
    private Double latitude;
    private Double longitude;
    private Double radiusKm;
}
