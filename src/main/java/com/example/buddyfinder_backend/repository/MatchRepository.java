package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.Match;
import com.example.buddyfinder_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m WHERE (m.user1.userId = :userId OR m.user2.userId = :userId) AND m.status = 'ACTIVE'")
    List<Match> findActiveMatchesByUserId(Long userId);

    @Query("SELECT m FROM Match m WHERE ((m.user1.userId = :user1Id AND m.user2.userId = :user2Id) OR (m.user1.userId = :user2Id AND m.user2.userId = :user1Id))")
    Optional<Match> findMatchBetweenUsers(Long user1Id, Long user2Id);
}