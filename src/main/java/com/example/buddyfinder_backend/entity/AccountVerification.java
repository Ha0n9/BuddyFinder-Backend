package com.example.buddyfinder_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long verificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentType documentType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String documentUrl; // Cloudinary URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VerificationStatus status = VerificationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User reviewedBy; // Admin who reviewed

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum DocumentType {
        POLICE_REPORT("Police Report"),
        PERSONAL_ID("Personal ID/Passport"),
        DRIVERS_LICENSE("Driver's License"),
        OTHER("Other Document");

        private final String displayName;

        DocumentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum VerificationStatus {
        PENDING("Pending Review"),
        APPROVED("Approved"),
        REJECTED("Rejected"),
        CANCELLED("Cancelled");

        private final String displayName;

        VerificationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}