package com.example.buddyfinder_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchId;

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    @JsonIgnoreProperties({"password", "likesGiven", "likesReceived", "matchesAsUser1", "matchesAsUser2", "messagesSent", "ratingsGiven", "ratingsReceived", "activitiesCreated"})
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    @JsonIgnoreProperties({"password", "likesGiven", "likesReceived", "matchesAsUser1", "matchesAsUser2", "messagesSent", "ratingsGiven", "ratingsReceived", "activitiesCreated"})
    private User user2;

    @CreationTimestamp
    private LocalDateTime matchedAt;

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.ACTIVE;

    private Float compatibilityScore;

    @UpdateTimestamp
    private LocalDateTime lastMessageAt;

    private Integer unreadCountUser;

    public enum MatchStatus {
        ACTIVE, INACTIVE, BLOCKED
    }
}