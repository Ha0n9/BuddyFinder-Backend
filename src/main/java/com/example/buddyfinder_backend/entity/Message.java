package com.example.buddyfinder_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    @JsonIgnoreProperties({"user1", "user2"})
    private Match match;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnoreProperties({"password", "likesGiven", "likesReceived", "matchesAsUser1", "matchesAsUser2", "messagesSent", "ratingsGiven", "ratingsReceived", "activitiesCreated"})
    private User sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String mediaUrl;

    private String mediaType;

    @CreationTimestamp
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Boolean isRead = false;

    private LocalDateTime readAt;

    @Column(nullable = false)
    private Boolean isDeleted = false;
}