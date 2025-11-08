package com.example.buddyfinder_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    // ID của entity liên quan (matchId, activityId, messageId, etc.)
    private Long relatedId;

    // Loại entity liên quan (MATCH, ACTIVITY, MESSAGE, etc.)
    @Column(length = 50)
    private String relatedType;

    @Column(nullable = false)
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        MATCH("You have a new match!"),
        MESSAGE("New message received"),
        ACTIVITY_INVITE("You've been invited to an activity"),
        ACTIVITY_JOINED("Someone joined your activity"),
        ACTIVITY_CANCELLED("Activity has been cancelled"),
        ACTIVITY_REMINDER("Activity reminder"),
        RATING_RECEIVED("You received a new rating"),
        SYSTEM("System notification"),
        ADMIN("Admin notification");

        private final String defaultMessage;

        NotificationType(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }
}