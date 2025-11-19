package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.entity.Report;
import com.example.buddyfinder_backend.entity.ReportMessage;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.ReportMessageRepository;
import com.example.buddyfinder_backend.repository.ReportRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import com.example.buddyfinder_backend.util.SanitizeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportMessageRepository reportMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public Map<String, Object> submitReport(Long reporterId, Map<String, Object> payload) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found"));
        Long reportedId = Optional.ofNullable(payload.get("reportedUserId"))
                .map(val -> Long.valueOf(val.toString()))
                .orElseThrow(() -> new RuntimeException("Reported user is required"));
        if (reportedId.equals(reporterId)) {
            throw new RuntimeException("You cannot report yourself");
        }
        User reported = userRepository.findById(reportedId)
                .orElseThrow(() -> new RuntimeException("Reported user not found"));

        Report report = Report.builder()
                .reporter(reporter)
                .reported(reported)
                .reason(SanitizeUtil.sanitize(Optional.ofNullable(payload.get("reason")).map(Object::toString).orElse("Other")))
                .description(SanitizeUtil.sanitize(Optional.ofNullable(payload.get("description")).map(Object::toString).orElse("")))
                .attachmentUrl(Optional.ofNullable(payload.get("attachmentUrl")).map(Object::toString).orElse(null))
                .status(Report.ReportStatus.OPEN)
                .build();

        Report saved = reportRepository.save(report);

        Object initMsg = payload.get("initialMessage");
        if (initMsg != null && !initMsg.toString().isBlank()) {
            addMessageInternal(saved, reporter, true, initMsg.toString(),
                    Optional.ofNullable(payload.get("attachmentUrl")).map(Object::toString).orElse(null));
        }

        return mapReport(saved);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getReportsFiledBy(Long userId) {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reportRepository.findByReporter(reporter).stream()
                .map(this::mapReport)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getReportsAgainst(Long userId) {
        User reported = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reportRepository.findByReported(reported).stream()
                .map(this::mapReport)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllReports() {
        return reportRepository.findAll().stream()
                .map(this::mapReport)
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> addMessage(Long reportId, Long senderId, Map<String, Object> payload) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!sender.getUserId().equals(report.getReporter().getUserId()) &&
                !sender.getUserId().equals(report.getReported().getUserId())) {
            throw new RuntimeException("You are not part of this report");
        }

        addMessageInternal(report, sender, sender.getUserId().equals(report.getReporter().getUserId()),
                SanitizeUtil.sanitize(Optional.ofNullable(payload.get("message")).map(Object::toString).orElse("")),
                payload.get("attachmentUrl") != null ? payload.get("attachmentUrl").toString() : null);

        return mapReport(reportRepository.findById(reportId).orElseThrow());
    }

    private void addMessageInternal(Report report, User sender, boolean fromReporter,
                                    String message, String attachmentUrl) {
        if ((message == null || message.isBlank()) && (attachmentUrl == null || attachmentUrl.isBlank())) {
            return;
        }
        ReportMessage msg = ReportMessage.builder()
                .report(report)
                .sender(sender)
                .fromReporter(fromReporter)
                .message(SanitizeUtil.sanitize(message))
                .attachmentUrl(attachmentUrl)
                .build();
        reportMessageRepository.save(msg);
    }

    @Transactional
    public Map<String, Object> updateStatus(Long reportId, Report.ReportStatus status, String adminNotes) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(status);
        if (status == Report.ReportStatus.RESOLVED) {
            report.setResolvedAt(LocalDateTime.now());
        }
        if (adminNotes != null) {
            report.setAdminNotes(adminNotes);
        }
        return mapReport(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReportById(Long reportId, Long requesterId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        if (!requesterId.equals(report.getReporter().getUserId()) &&
                !requesterId.equals(report.getReported().getUserId())) {
            throw new RuntimeException("You are not part of this report");
        }
        return mapReport(report);
    }

    private Map<String, Object> mapReport(Report report) {
        Map<String, Object> result = new HashMap<>();
        result.put("reportId", report.getReportId());
        result.put("reason", report.getReason());
        result.put("description", report.getDescription());
        result.put("attachmentUrl", report.getAttachmentUrl());
        result.put("status", report.getStatus().name());
        result.put("createdAt", report.getCreatedAt());
        result.put("updatedAt", report.getUpdatedAt());
        result.put("resolvedAt", report.getResolvedAt());
        result.put("adminNotes", report.getAdminNotes());
        result.put("reporter", Map.of(
                "userId", report.getReporter().getUserId(),
                "name", report.getReporter().getName(),
                "email", report.getReporter().getEmail()
        ));
        result.put("reported", Map.of(
                "userId", report.getReported().getUserId(),
                "name", report.getReported().getName(),
                "email", report.getReported().getEmail()
        ));
        result.put("messages", reportMessageRepository.findByReportOrderByCreatedAtAsc(report).stream()
                .map(msg -> Map.of(
                        "id", msg.getId(),
                        "fromReporter", msg.getFromReporter(),
                        "message", msg.getMessage(),
                        "attachmentUrl", msg.getAttachmentUrl(),
                        "createdAt", msg.getCreatedAt(),
                        "senderName", msg.getSender().getName()
                )).collect(Collectors.toList()));
        return result;
    }
}
