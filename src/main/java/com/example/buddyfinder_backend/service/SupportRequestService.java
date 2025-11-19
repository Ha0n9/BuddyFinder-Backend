package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.entity.SupportRequest;
import com.example.buddyfinder_backend.repository.SupportRequestRepository;
import com.example.buddyfinder_backend.util.SanitizeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportRequestService {

    private final SupportRequestRepository supportRequestRepository;

    @Transactional
    public SupportRequest createRequest(String email, String message) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }

        SupportRequest request = SupportRequest.builder()
                .email(SanitizeUtil.sanitize(email.trim()))
                .message(SanitizeUtil.sanitize(message.trim()))
                .status(SupportRequest.Status.OPEN)
                .build();
        return supportRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<SupportRequest> getAllRequests() {
        return supportRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public SupportRequest updateStatus(Long requestId, SupportRequest.Status status, String adminNotes) {
        SupportRequest request = supportRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Support request not found"));
        request.setStatus(status);
        if (status == SupportRequest.Status.RESOLVED) {
            request.setHandledAt(LocalDateTime.now());
        }
        if (adminNotes != null) {
            request.setAdminNotes(SanitizeUtil.sanitize(adminNotes));
        }
        return supportRequestRepository.save(request);
    }
}
