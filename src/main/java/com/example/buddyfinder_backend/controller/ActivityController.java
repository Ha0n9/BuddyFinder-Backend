package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.entity.Activity;
import com.example.buddyfinder_backend.service.ActivityService;
import com.example.buddyfinder_backend.service.GroupChatService;
import com.example.buddyfinder_backend.dto.ChatRoomInfoDto;
import com.example.buddyfinder_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final GroupChatService groupChatService;
    private final JwtUtil jwtUtil;

    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String token = authHeader.substring(7);
        try {
            return jwtUtil.extractUserId(token);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    @PostMapping("/create")
    public Activity createActivity(
            @RequestHeader("Authorization") String auth,
            @RequestBody Activity activity
    ) {
        Long creatorId = extractUserId(auth);
        return activityService.createActivity(creatorId, activity);
    }

    @PostMapping("/{activityId}/join")
    public String joinActivity(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long activityId
    ) {
        Long userId = extractUserId(auth);
        return activityService.joinActivity(activityId, userId);
    }

    @PostMapping("/{activityId}/leave")
    public String leaveActivity(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long activityId
    ) {
        Long userId = extractUserId(auth);
        return activityService.leaveActivity(activityId, userId);
    }

    @GetMapping("/{activityId}/chat-room")
    public ChatRoomInfoDto getChatRoom(@PathVariable Long activityId) {
        return groupChatService.getRoomInfoByActivity(activityId);
    }

    @GetMapping("")
    public List<Activity> getAll() {
        return activityService.getAllActivities();
    }
}
