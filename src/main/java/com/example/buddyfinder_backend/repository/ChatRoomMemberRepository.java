package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    boolean existsByChatRoom_IdAndUser_UserId(Long roomId, Long userId);

    Optional<ChatRoomMember> findByChatRoom_IdAndUser_UserId(Long roomId, Long userId);

    List<ChatRoomMember> findByChatRoom_Id(Long roomId);

    void deleteByChatRoom_IdAndUser_UserId(Long roomId, Long userId);

    List<ChatRoomMember> findByUser_UserId(Long userId);
}
