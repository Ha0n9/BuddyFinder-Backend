package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByMatch_MatchIdOrderByTimestampAsc(Long matchId);

    Integer countByMatch_MatchIdAndIsReadFalseAndSender_UserIdNot(Long matchId, Long userId);
}
