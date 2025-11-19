package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.ChatRoomInfoDto;
import com.example.buddyfinder_backend.dto.ChatRoomMemberDto;
import com.example.buddyfinder_backend.dto.GroupChatMessageRequest;
import com.example.buddyfinder_backend.dto.GroupChatMessageResponse;
import com.example.buddyfinder_backend.entity.*;
import com.example.buddyfinder_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ========== ROOM INIT ==========

    @Transactional
    public ChatRoom createRoomForActivity(Long activityId, Long ownerId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        ChatRoom room = ChatRoom.builder()
                .activity(activity)
                .createdAt(LocalDateTime.now())
                .build();
        room = chatRoomRepository.save(room);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner user not found"));

        ChatRoomMember ownerMember = ChatRoomMember.builder()
                .chatRoom(room)
                .user(owner)
                .role(ChatRoomMember.Role.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();
        chatRoomMemberRepository.save(ownerMember);

        // optional: tạo system message
        GroupMessage systemMsg = GroupMessage.builder()
                .chatRoom(room)
                .sender(null)
                .content(owner.getName() + " created this group.")
                .systemMessage(true)
                .timestamp(LocalDateTime.now())
                .build();
        groupMessageRepository.save(systemMsg);

        return room;
    }

    public ChatRoomInfoDto getRoomInfoByActivity(Long activityId) {
        ChatRoom room = chatRoomRepository.findByActivity_ActivityId(activityId)
                .orElseThrow(() -> new RuntimeException("Chat room not found for activity"));

        return ChatRoomInfoDto.builder()
                .roomId(room.getId())
                .activityId(activityId)
                .activityTitle(room.getActivity().getTitle())
                .build();
    }

    public List<ChatRoomInfoDto> getRoomsForUser(Long userId) {
        List<ChatRoomMember> memberships = chatRoomMemberRepository.findByUser_UserId(userId);

        return memberships.stream()
                .map(member -> {
                    ChatRoom room = member.getChatRoom();
                    Activity activity = room.getActivity();
                    return ChatRoomInfoDto.builder()
                            .roomId(room.getId())
                            .activityId(activity != null ? activity.getActivityId() : null)
                            .activityTitle(activity != null ? activity.getTitle() : "Group Chat")
                            .build();
                })
                .sorted(Comparator.comparing(
                        ChatRoomInfoDto::getRoomId,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    // ========== JOIN / LEAVE ROOM ==========

    @Transactional
    public void joinRoomByActivity(Long activityId, Long userId) {
        ChatRoom room = chatRoomRepository.findByActivity_ActivityId(activityId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        if (chatRoomMemberRepository.existsByChatRoom_IdAndUser_UserId(room.getId(), userId)) {
            return; // đã là member rồi
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        chatRoomMemberRepository.save(
                ChatRoomMember.builder()
                        .chatRoom(room)
                        .user(user)
                        .role(ChatRoomMember.Role.MEMBER)
                        .joinedAt(LocalDateTime.now())
                        .build()
        );

        // system message
        GroupMessage system = GroupMessage.builder()
                .chatRoom(room)
                .sender(null)
                .content(user.getName() + " has joined the chat.")
                .systemMessage(true)
                .timestamp(LocalDateTime.now())
                .build();
        groupMessageRepository.save(system);
    }

    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        ChatRoomMember member = chatRoomMemberRepository
                .findByChatRoom_IdAndUser_UserId(roomId, userId)
                .orElseThrow(() -> new RuntimeException("User is not in this room"));

        chatRoomMemberRepository.delete(member);

        ChatRoom room = member.getChatRoom();
        User user = member.getUser();

        GroupMessage system = GroupMessage.builder()
                .chatRoom(room)
                .sender(null)
                .content(user.getName() + " left the chat.")
                .systemMessage(true)
                .timestamp(LocalDateTime.now())
                .build();
        groupMessageRepository.save(system);
    }

    // ========== MESSAGES ==========

    @Transactional
    public GroupChatMessageResponse sendMessage(Long roomId, GroupChatMessageRequest req) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        if (!chatRoomMemberRepository.existsByChatRoom_IdAndUser_UserId(roomId, req.getSenderId())) {
            throw new RuntimeException("User is not a member of this room");
        }

        User sender = userRepository.findById(req.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        GroupMessage msg = GroupMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(req.getContent())
                .systemMessage(false)
                .timestamp(LocalDateTime.now())
                .build();

        msg = groupMessageRepository.save(msg);

        // notify other members
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoom_Id(roomId);
        String activityTitle = room.getActivity() != null ? room.getActivity().getTitle() : "Group chat";
        for (ChatRoomMember member : members) {
            Long memberId = member.getUser().getUserId();
            if (!memberId.equals(sender.getUserId())) {
                notificationService.notifyGroupMessage(memberId, roomId, sender.getName(), activityTitle);
            }
        }

        return GroupChatMessageResponse.builder()
                .id(msg.getId())
                .roomId(roomId)
                .senderId(sender.getUserId())
                .senderName(sender.getName())
                .content(msg.getContent())
                .systemMessage(false)
                .timestamp(msg.getTimestamp())
                .build();
    }

    public List<GroupChatMessageResponse> getMessages(Long roomId) {
        List<GroupMessage> list = groupMessageRepository
                .findByChatRoom_IdOrderByTimestampAsc(roomId);

        return list.stream()
                .map(m -> GroupChatMessageResponse.builder()
                        .id(m.getId())
                        .roomId(roomId)
                        .senderId(m.getSender() != null ? m.getSender().getUserId() : null)
                        .senderName(m.getSender() != null ? m.getSender().getName() : "System")
                        .content(m.getContent())
                        .systemMessage(m.isSystemMessage())
                        .timestamp(m.getTimestamp())
                        .build())
                .toList();
    }

    public List<ChatRoomMemberDto> getMembers(Long roomId) {
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoom_Id(roomId);

        return members.stream()
                .map(m -> ChatRoomMemberDto.builder()
                        .userId(m.getUser().getUserId())
                        .name(m.getUser().getName())
                        .role(m.getRole())
                        .build())
                .toList();
    }
}
