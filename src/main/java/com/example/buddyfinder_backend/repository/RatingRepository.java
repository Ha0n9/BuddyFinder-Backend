package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByToUser_UserId(Long toUserId);

    Optional<Rating> findByFromUser_UserIdAndToUser_UserId(Long fromUserId, Long toUserId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.toUser.userId = :userId")
    Double getAverageRating(Long userId);

    // === 🆕 DELETE METHODS FOR GDPR COMPLIANCE ===
    void deleteByFromUser_UserId(Long fromUserId);
    void deleteByToUser_UserId(Long toUserId);
}