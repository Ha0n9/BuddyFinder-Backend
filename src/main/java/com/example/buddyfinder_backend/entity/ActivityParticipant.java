package com.example.buddyfinder_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long participantId;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({
            "password",
            "likesGiven",
            "likesReceived",
            "matchesAsUser1",
            "matchesAsUser2",
            "messagesSent",
            "ratingsGiven",
            "ratingsReceived",
            "activitiesCreated"
    })
    private User user;

    @CreationTimestamp
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    private ParticipantStatus status = ParticipantStatus.JOINED;

    private Boolean attendance;

    private Integer ratingGiven;

    public enum ParticipantStatus {
        JOINED, CANCELLED, COMPLETED
    }
}
