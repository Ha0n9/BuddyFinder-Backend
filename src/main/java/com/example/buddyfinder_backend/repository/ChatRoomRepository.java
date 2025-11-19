package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByActivity_ActivityId(Long activityId);
}
