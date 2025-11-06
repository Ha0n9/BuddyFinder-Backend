package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {
    Optional<Likes> findByFromUser_UserIdAndToUser_UserId(Long fromUserId, Long toUserId);
    Boolean existsByFromUser_UserIdAndToUser_UserId(Long fromUserId, Long toUserId);
}
