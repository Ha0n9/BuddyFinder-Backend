package com.example.buddyfinder_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @JsonIgnoreProperties({"password", "likesGiven", "likesReceived", "matchesAsUser1", "matchesAsUser2"})
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", referencedColumnName = "userId", nullable = false)
    @JsonIgnoreProperties({"password", "likesGiven", "likesReceived", "matchesAsUser1", "matchesAsUser2"})
    private User reported;

    // Legacy column kept for backward compatibility with existing schema
    @Column(name = "reported_id", nullable = false)
    private Long legacyReportedId;

    @Column(nullable = false, length = 100)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String attachmentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportStatus status = ReportStatus.OPEN;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;

    private String adminNotes;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"report"})
    private List<ReportMessage> messages;

    @PrePersist
    @PreUpdate
    private void syncLegacyColumns() {
        if (reported != null) {
            legacyReportedId = reported.getUserId();
        }
    }

    public enum ReportStatus {
        OPEN,
        UNDER_REVIEW,
        ACTION_TAKEN,
        RESOLVED
    }
}
