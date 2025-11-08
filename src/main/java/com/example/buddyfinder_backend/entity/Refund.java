package com.example.buddyfinder_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    // ID của subscription (nếu có subscription entity)
    private Long subId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RefundMethod refundMethod;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal originalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RefundType refundType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RefundReason reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(length = 100)
    private String paymentGateway;

    // Original transaction ID from payment gateway
    private String originalTransId;

    // Refund transaction ID from payment gateway
    private String refundTransId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User processedBy;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime processedAt;

    private LocalDateTime completedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum RefundMethod {
        ORIGINAL_PAYMENT,  // Hoàn về phương thức thanh toán gốc
        CREDIT,           // Credit vào tài khoản
        BANK_TRANSFER,    // Chuyển khoản ngân hàng
        OTHER
    }

    public enum RefundType {
        FULL,            // Hoàn toàn bộ
        PARTIAL,         // Hoàn một phần
        SUBSCRIPTION_CANCELLATION  // Hoàn do hủy subscription
    }

    public enum RefundReason {
        NOT_SATISFIED("Not satisfied with service"),
        TECHNICAL_ISSUES("Technical issues"),
        ACCIDENTAL_PURCHASE("Accidental purchase"),
        DUPLICATE_CHARGE("Duplicate charge"),
        SERVICE_NOT_AVAILABLE("Service not available in area"),
        PRIVACY_CONCERNS("Privacy concerns"),
        OTHER("Other reason");

        private final String description;

        RefundReason(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum RefundStatus {
        PENDING,         // Chờ xử lý
        UNDER_REVIEW,    // Đang xem xét
        APPROVED,        // Đã chấp thuận
        PROCESSING,      // Đang xử lý hoàn tiền
        COMPLETED,       // Hoàn tất
        REJECTED,        // Từ chối
        CANCELLED        // Người dùng hủy yêu cầu
    }
}