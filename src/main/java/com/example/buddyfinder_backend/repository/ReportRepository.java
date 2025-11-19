package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.Report;
import com.example.buddyfinder_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReporter(User reporter);
    List<Report> findByReported(User reported);
}
