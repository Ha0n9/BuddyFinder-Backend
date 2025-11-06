package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByCreator_UserId(Long creatorId);

    @Query("SELECT a FROM Activity a WHERE a.isCancelled = false AND a.scheduledTime > :now ORDER BY a.scheduledTime")
    List<Activity> findUpcomingActivities(LocalDateTime now);

    List<Activity> findByLocationContainingIgnoreCase(String location);
}