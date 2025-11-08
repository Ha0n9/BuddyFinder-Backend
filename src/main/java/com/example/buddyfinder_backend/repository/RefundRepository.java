package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    // Tìm refunds theo user
    List<Refund> findByUser_UserIdOrderByRequestedAtDesc(Long userId);

    // Tìm refunds theo status
    List<Refund> findByStatusOrderByRequestedAtDesc(Refund.RefundStatus status);

    // Tìm refunds pending/under_review (cho admin)
    @Query("SELECT r FROM Refund r WHERE r.status IN ('PENDING', 'UNDER_REVIEW') ORDER BY r.requestedAt ASC")
    List<Refund> findPendingRefunds();

    // Tìm refund theo original transaction ID
    Optional<Refund> findByOriginalTransId(String originalTransId);

    // Check xem user đã request refund cho transaction chưa
    boolean existsByUser_UserIdAndOriginalTransId(Long userId, String originalTransId);

    // Thống kê refunds theo thời gian
    @Query("SELECT COUNT(r) FROM Refund r WHERE r.requestedAt BETWEEN :startDate AND :endDate")
    Long countRefundsByDateRange(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    // Tổng số tiền refund theo status
    @Query("SELECT SUM(r.originalAmount) FROM Refund r WHERE r.status = :status")
    java.math.BigDecimal sumAmountByStatus(@Param("status") Refund.RefundStatus status);
}