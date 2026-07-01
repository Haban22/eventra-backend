package com.eventra.backend.module.config.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "system_config")
@Getter
@Setter
public class SystemConfig {
    @Id
    private short id = 1;

    @Column(name = "cancellation_window_hours", nullable = false)
    private int cancellationWindowHours;

    @Column(name = "ticket_hold_timeout_minutes", nullable = false)
    private int ticketHoldTimeoutMinutes;

    @Column(name = "platform_fee_percentage", nullable = false)
    private BigDecimal platformFeePercentage;

    @Column(name = "min_payout_amount", nullable = false)
    private BigDecimal minPayoutAmount;

    @Column(name = "auto_approve_payout_threshold", nullable = false)
    private BigDecimal autoApprovePayoutThreshold;

    @Column(name = "ai_recommendations_enabled", nullable = false)
    private boolean aiRecommendationsEnabled;

    @Column(name = "ai_chat_enabled", nullable = false)
    private boolean aiChatEnabled;

    @Column(name = "ai_fraud_detection_enabled", nullable = false)
    private boolean aiFraudDetectionEnabled;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
