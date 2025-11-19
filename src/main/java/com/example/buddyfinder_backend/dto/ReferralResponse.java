package com.example.buddyfinder_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralResponse {
    private String referralCode;
    private String referralLink;
    private Long totalInvited;
    private Long acceptedCount;
    private Boolean canClaimReward;
    private Boolean rewardClaimed;
    private Boolean featureLocked;
    private List<ReferralDetail> invites;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferralDetail {
        private Long referralId;
        private String referredEmail;
        private String referredName;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime acceptedAt;
    }
}
