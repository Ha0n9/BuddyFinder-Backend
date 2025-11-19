package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.entity.*;
import com.example.buddyfinder_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final GroupChatService groupChatService;  // <-- New service

    /**
     * Create a new activity and automatically create a group chat room for it.
     */
    @Transactional
    public Activity createActivity(Long creatorId, Activity activity) {

        // Validate creator
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        // Assign creator and default values
        activity.setCreator(creator);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setCurrentCount(1);
        activity.setIsCancelled(false); // ensure DB not-null constraint satisfied

        // ❌ Removed: activity.setCancelled(false);
        // Because your Activity entity does NOT contain a "cancelled" field

        // Save activity first
        Activity savedActivity = activityRepository.save(activity);

        // Automatically add creator as participant
        ActivityParticipant creatorParticipant = ActivityParticipant.builder()
                .activity(savedActivity)
                .user(creator)
                .joinedAt(LocalDateTime.now())
                .build();
        activityParticipantRepository.save(creatorParticipant);

        // 🔥 Create group chat room associated with this activity
        groupChatService.createRoomForActivity(savedActivity.getActivityId(), creatorId);

        return savedActivity;
    }

    /**
     * Join an activity and automatically join its group chat room.
     */
    @Transactional
    public String joinActivity(Long activityId, Long userId) {
        try {
            Activity activity = activityRepository.findById(activityId)
                    .orElseThrow(() -> new RuntimeException("Activity not found"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (activityParticipantRepository.existsByActivity_ActivityIdAndUser_UserId(activityId, userId)) {
                throw new IllegalStateException("You are already a participant of this activity.");
            }

            int currentCount = activity.getCurrentCount() != null ? activity.getCurrentCount() : 0;
            if (currentCount >= activity.getMaxParticipants()) {
                throw new IllegalStateException("Activity is full.");
            }

            // Add participant
            ActivityParticipant participant = ActivityParticipant.builder()
                    .activity(activity)
                    .user(user)
                    .joinedAt(LocalDateTime.now())
                    .build();
            activityParticipantRepository.save(participant);

            activity.setCurrentCount(currentCount + 1);
            activityRepository.save(activity);

            try {
                groupChatService.joinRoomByActivity(activityId, userId);
            } catch (Exception e) {
                System.out.println("Failed to auto join chat room, but activity join is successful.");
            }


            return "Joined activity successfully.";

        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    /**
     * Leave an activity and automatically leave the group chat room as well.
     */
    @Transactional
    public String leaveActivity(Long activityId, Long userId) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        // Check if user is a participant
        ActivityParticipant participant =
                activityParticipantRepository.findByActivity_ActivityIdAndUser_UserId(activityId, userId)
                        .orElseThrow(() -> new RuntimeException("You are not a participant of this activity."));

        // Remove participant association before delete to avoid stale references
        if (activity.getParticipants() != null) {
            activity.getParticipants().removeIf(p ->
                    p.getParticipantId().equals(participant.getParticipantId()));
        }
        activityParticipantRepository.delete(participant);

        // Decrease participant count
        int currentCount = activity.getCurrentCount() != null ? activity.getCurrentCount() : 0;
        activity.setCurrentCount(Math.max(0, currentCount - 1));
        activityRepository.save(activity);

        // Auto leave group chat
        chatRoomRepository.findByActivity_ActivityId(activityId).ifPresent(room ->
                groupChatService.leaveRoom(room.getId(), userId)
        );

        return "You have left the activity.";
    }

    /**
     * Get all activities.
     */
    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    /**
     * Get activity by ID.
     */
    public Activity getActivity(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
    }
}
