package com.example.buddyfinder_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String photos; // JSON string array

    @Column(columnDefinition = "TEXT")
    private String fitnessGoals;

    @Column(columnDefinition = "TEXT")
    private String preferredActivities;

    private Integer workoutFrequency;

    @Column(columnDefinition = "TEXT")
    private String experienceLevel;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    private String gymLocation;

    @Column(columnDefinition = "TEXT")
    private String workoutTimePref;
}
