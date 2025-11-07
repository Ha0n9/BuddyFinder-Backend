package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.entity.Activity;
import com.example.buddyfinder_backend.entity.ActivityParticipant;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.ActivityParticipantRepository;
import com.example.buddyfinder_backend.repository.ActivityRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActivityParticipantRepository participantRepository;

    public Activity createActivity(Activity activity, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        activity.setCreator(creator);
        activity.setCurrentCount(0);
        activity.setIsCancelled(false);

        return activityRepository.save(activity);
    }

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public Activity getActivityById(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
    }

    public void joinActivity(Long activityId, Long userId) {
        Activity activity = getActivityById(activityId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if already joined
        boolean alreadyJoined = participantRepository.existsByActivity_ActivityIdAndUser_UserId(activityId, userId);
        if (alreadyJoined) {
            throw new RuntimeException("You have already joined this activity");
        }

        // Check if full
        if (activity.getCurrentCount() >= activity.getMaxParticipants()) {
            throw new RuntimeException("Activity is full");
        }

        // Create participant
        ActivityParticipant participant = ActivityParticipant.builder()
                .activity(activity)
                .user(user)
                .status(ActivityParticipant.ParticipantStatus.valueOf("JOINED"))
                .build();

        participantRepository.save(participant);

        // Update count
        activity.setCurrentCount(activity.getCurrentCount() + 1);
        activityRepository.save(activity);
    }

    /**
     * FIX: Delete activity with proper cascade handling
     * Delete all participants first, then delete the activity
     */
    @Transactional
    public void deleteActivity(Long activityId, Long userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        // Check: Only creator can delete
        if (!activity.getCreator().getUserId().equals(userId)) {
            throw new RuntimeException("You can only delete your own activities");
        }

        // Step 1: Delete all participants first
        List<ActivityParticipant> participants = participantRepository.findByActivity_ActivityId(activityId);
        if (!participants.isEmpty()) {
            participantRepository.deleteAll(participants);
            System.out.println("✅ Deleted " + participants.size() + " participants");
        }

        // Step 2: Now delete the activity
        activityRepository.delete(activity);
        System.out.println("✅ Activity deleted successfully");
    }
}