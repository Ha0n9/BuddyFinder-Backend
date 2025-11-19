package com.example.buddyfinder_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    @JsonIgnoreProperties({"messages"})
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnoreProperties({"password", "likesGiven", "likesReceived", "matchesAsUser1", "matchesAsUser2"})
    private User sender;

    @Column(nullable = false)
    private Boolean fromReporter;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String attachmentUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
