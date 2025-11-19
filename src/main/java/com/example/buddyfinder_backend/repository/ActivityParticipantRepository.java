package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.ActivityParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Long> {

    // Check if user already joined activity
    boolean existsByActivity_ActivityIdAndUser_UserId(Long activityId, Long userId);

    // Get all participants of an activity
    List<ActivityParticipant> findByActivity_ActivityId(Long activityId);

    // Get all activities a user joined
    List<ActivityParticipant> findByUser_UserId(Long userId);

    // === 🆕 REQUIRED FOR leaveActivity() ===
    Optional<ActivityParticipant> findByActivity_ActivityIdAndUser_UserId(Long activityId, Long userId);

    // === DELETE user's activity history (if needed) ===
    void deleteByUser_UserId(Long userId);
}
