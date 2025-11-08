package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Lấy tất cả notifications của user (mới nhất trước)
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    // Lấy unread notifications
    List<Notification> findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // Đếm số unread notifications
    Long countByUser_UserIdAndIsReadFalse(Long userId);

    // Mark tất cả notifications của user là đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    // Delete notifications cũ hơn X ngày
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :date")
    void deleteOldNotifications(@Param("date") java.time.LocalDateTime date);

    // Lấy notifications theo type
    List<Notification> findByUser_UserIdAndTypeOrderByCreatedAtDesc(Long userId, Notification.NotificationType type);
}