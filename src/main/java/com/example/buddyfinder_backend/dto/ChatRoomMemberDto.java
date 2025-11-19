package com.example.buddyfinder_backend.dto;

import com.example.buddyfinder_backend.entity.ChatRoomMember;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRoomMemberDto {
    private Long userId;
    private String name;
    private ChatRoomMember.Role role;
}
