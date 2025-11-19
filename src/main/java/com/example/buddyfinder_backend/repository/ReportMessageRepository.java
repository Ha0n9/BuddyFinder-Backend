package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.Report;
import com.example.buddyfinder_backend.entity.ReportMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportMessageRepository extends JpaRepository<ReportMessage, Long> {
    List<ReportMessage> findByReportOrderByCreatedAtAsc(Report report);
}
