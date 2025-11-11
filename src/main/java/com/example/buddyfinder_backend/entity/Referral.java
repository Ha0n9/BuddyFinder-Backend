package com.example.buddyfinder_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "referrals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long referralId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User referrer; // User who sent the invite

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User referred; // User who signed up using the code (null until signup)

    @Column(nullable = false, unique = true, length = 20)
    private String referralCode; // Unique code for referrer (e.g., "USER123ABC")

    @Column(length = 100)
    private String referredEmail; // Email of invited person (before they signup)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReferralStatus status = ReferralStatus.PENDING;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime acceptedAt; // When referred user signed up

    @Column(nullable = false)
    private Boolean rewardClaimed = false; // Whether referrer got their reward

    private LocalDateTime rewardClaimedAt;

    public enum ReferralStatus {
        PENDING,    // Invited but not signed up yet
        ACCEPTED,   // Invited person signed up
        EXPIRED,    // Invite expired (optional)
        CANCELLED   // Referrer cancelled invite
    }
}