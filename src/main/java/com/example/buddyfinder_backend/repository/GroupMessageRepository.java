package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {

    List<GroupMessage> findByChatRoom_IdOrderByTimestampAsc(Long roomId);
}
