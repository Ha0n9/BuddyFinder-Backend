package com.example.buddyfinder_backend.controller;

import com.example.buddyfinder_backend.dto.ChatRoomInfoDto;
import com.example.buddyfinder_backend.dto.ChatRoomMemberDto;
import com.example.buddyfinder_backend.dto.GroupChatMessageResponse;
import com.example.buddyfinder_backend.service.GroupChatService;
import com.example.buddyfinder_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/group-chat")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;
    private final JwtUtil jwtUtil;

    private Long getUserIdFromAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }

    @GetMapping("/rooms/by-activity/{activityId}")
    public ResponseEntity<ChatRoomInfoDto> getRoomByActivity(
            @PathVariable Long activityId
    ) {
        return ResponseEntity.ok(groupChatService.getRoomInfoByActivity(activityId));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<GroupChatMessageResponse>> getMessages(
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(groupChatService.getMessages(roomId));
    }

    @GetMapping("/rooms/{roomId}/members")
    public ResponseEntity<List<ChatRoomMemberDto>> getMembers(
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(groupChatService.getMembers(roomId));
    }

    @GetMapping("/rooms/me")
    public ResponseEntity<List<ChatRoomInfoDto>> getMyRooms(
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = getUserIdFromAuth(authHeader);
        return ResponseEntity.ok(groupChatService.getRoomsForUser(userId));
    }

    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long roomId
    ) {
        Long userId = getUserIdFromAuth(authHeader);
        groupChatService.leaveRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }
}
