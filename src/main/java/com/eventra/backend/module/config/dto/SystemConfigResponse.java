package com.eventra.backend.module.config.dto;

import com.eventra.backend.module.config.entity.SystemConfig;

import java.math.BigDecimal;
import java.time.Instant;

public record SystemConfigResponse(
        int cancellationWindowHours,
        int ticketHoldTimeoutMinutes,
        BigDecimal platformFeePercentage,
        BigDecimal minPayoutAmount,
        BigDecimal autoApprovePayoutThreshold,
        boolean aiRecommendationsEnabled,
        boolean aiChatEnabled,
        boolean aiFraudDetectionEnabled,
        Instant updatedAt
) {
    public static SystemConfigResponse from(SystemConfig c) {
        return new SystemConfigResponse(
                c.getCancellationWindowHours(),
                c.getTicketHoldTimeoutMinutes(),
                c.getPlatformFeePercentage(),
                c.getMinPayoutAmount(),
                c.getAutoApprovePayoutThreshold(),
                c.isAiRecommendationsEnabled(),
                c.isAiChatEnabled(),
                c.isAiFraudDetectionEnabled(),
                c.getUpdatedAt()
        );
    }
}
