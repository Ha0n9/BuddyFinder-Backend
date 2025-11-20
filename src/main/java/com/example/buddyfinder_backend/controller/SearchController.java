package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.SearchFilters;
import com.example.buddyfinder_backend.dto.UserResponse;
import com.example.buddyfinder_backend.security.JwtUtil;
import com.example.buddyfinder_backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final JwtUtil jwtUtil;

    @GetMapping("/buddies")
    public ResponseEntity<List<UserResponse>> searchBuddies(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String interests,
            @RequestParam(required = false) String activity,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String mbtiType,
            @RequestParam(required = false) String zodiacSign,
            @RequestParam(required = false) String fitnessLevel,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radiusKm) {

        Long userId = extractUserIdFromToken(authHeader);
        SearchFilters filters = buildFilters(location, interests, activity, time, mbtiType, zodiacSign, fitnessLevel, gender, latitude, longitude, radiusKm);
        return ResponseEntity.ok(searchService.searchWithFilters(userId, filters));
    }

    @GetMapping("/potential")
    public ResponseEntity<List<UserResponse>> getPotentialMatches(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String interests,
            @RequestParam(required = false) String activity,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String mbtiType,
            @RequestParam(required = false) String zodiacSign,
            @RequestParam(required = false) String fitnessLevel,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radiusKm) {

        Long userId = extractUserIdFromToken(authHeader);
        SearchFilters filters = buildFilters(location, interests, activity, time, mbtiType, zodiacSign, fitnessLevel, gender, latitude, longitude, radiusKm);
        return ResponseEntity.ok(searchService.getPotentialMatches(userId, filters));
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }

    private SearchFilters buildFilters(String location,
                                       String interests,
                                       String activity,
                                       String time,
                                       String mbtiType,
                                       String zodiacSign,
                                       String fitnessLevel,
                                       String gender,
                                       Double latitude,
                                       Double longitude,
                                       Double radiusKm) {
        return SearchFilters.builder()
                .location(location)
                .interests(interests)
                .activity(activity)
                .time(time)
                .mbtiType(mbtiType)
                .zodiacSign(zodiacSign)
                .fitnessLevel(fitnessLevel)
                .gender(gender)
                .latitude(latitude)
                .longitude(longitude)
                .radiusKm(radiusKm)
                .build();
    }
}
