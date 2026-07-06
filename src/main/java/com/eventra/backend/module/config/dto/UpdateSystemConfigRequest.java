package com.eventra.backend.module.config.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

// All fields optional (nullable) — PATCH semantics: only provided fields are updated.
public record UpdateSystemConfigRequest(
        @Min(0) Integer cancellationWindowHours,
        @Min(0) Integer ticketHoldTimeoutMinutes,
        @DecimalMin("0.0") BigDecimal platformFeePercentage,
        @DecimalMin("0.0") BigDecimal minPayoutAmount,
        @DecimalMin("0.0") BigDecimal autoApprovePayoutThreshold,
        Boolean aiRecommendationsEnabled,
        Boolean aiChatEnabled,
        Boolean aiFraudDetectionEnabled
) {
}
