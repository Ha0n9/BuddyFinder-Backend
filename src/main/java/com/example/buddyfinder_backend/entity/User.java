package com.example.buddyfinder_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private Integer age;

    @Column(length = 20)
    private String gender;

    @Column(columnDefinition = "TEXT")
    private String interests;

    private String location;

    private Float latitude;

    private Float longitude;

    private Boolean availability;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "TEXT")
    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TierType tier = TierType.FREE;

    private String zodiacSign;

    private String mbtiType;

    private String fitnessLevel;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime lastLogin;

    @Column(nullable = false)
    private Boolean isActive = true;

    private LocalDateTime banUntil;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column(nullable = false)
    private Boolean isAdmin = false;

    // FIX: Add @JsonIgnoreProperties to ALL relationships
    @OneToMany(mappedBy = "fromUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"fromUser", "toUser"})
    private List<Likes> likesGiven;

    @OneToMany(mappedBy = "toUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"fromUser", "toUser"})
    private List<Likes> likesReceived;

    @OneToMany(mappedBy = "user1", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"user1", "user2"})
    private List<Match> matchesAsUser1;

    @OneToMany(mappedBy = "user2", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"user1", "user2"})
    private List<Match> matchesAsUser2;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"sender", "receiver", "match"})
    private List<Message> messagesSent;

    @OneToMany(mappedBy = "fromUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"fromUser", "toUser"})
    private List<Rating> ratingsGiven;

    @OneToMany(mappedBy = "toUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"fromUser", "toUser"})
    private List<Rating> ratingsReceived;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"creator"})
    private List<Activity> activitiesCreated;

    public enum TierType {
        FREE, PREMIUM, ELITE
    }
}
