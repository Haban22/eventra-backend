package com.eventra.backend.module.config.service;

import com.eventra.backend.module.auth.service.AuditService;
import com.eventra.backend.module.config.dto.SystemConfigResponse;
import com.eventra.backend.module.config.dto.UpdateSystemConfigRequest;
import com.eventra.backend.module.config.entity.SystemConfig;
import com.eventra.backend.module.config.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

// Single-row config table (id is always 1, seeded by V9). Read is cheap enough
// (one PK lookup) that a separate cache layer isn't worth the invalidation
// complexity — callers (booking cancellation window, ticket hold timeout) just
// call getConfig() directly.
@Service
public class SystemConfigService {
    private final SystemConfigRepository repository;
    private final AuditService auditService;

    public SystemConfigService(SystemConfigRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public SystemConfig getConfig() {
        return repository.findById((short) 1)
                .orElseThrow(() -> new IllegalStateException("system_config row missing — V9 migration should have seeded it"));
    }

    @Transactional(readOnly = true)
    public SystemConfigResponse getConfigResponse() {
        return SystemConfigResponse.from(getConfig());
    }

    @Transactional
    public SystemConfigResponse update(UUID adminId, UpdateSystemConfigRequest req, String ipAddress) {
        SystemConfig config = getConfig();
        StringBuilder changes = new StringBuilder();

        if (req.cancellationWindowHours() != null) {
            changes.append("cancellationWindowHours: ").append(config.getCancellationWindowHours()).append(" -> ").append(req.cancellationWindowHours()).append("; ");
            config.setCancellationWindowHours(req.cancellationWindowHours());
        }
        if (req.ticketHoldTimeoutMinutes() != null) {
            changes.append("ticketHoldTimeoutMinutes: ").append(config.getTicketHoldTimeoutMinutes()).append(" -> ").append(req.ticketHoldTimeoutMinutes()).append("; ");
            config.setTicketHoldTimeoutMinutes(req.ticketHoldTimeoutMinutes());
        }
        if (req.platformFeePercentage() != null) {
            changes.append("platformFeePercentage: ").append(config.getPlatformFeePercentage()).append(" -> ").append(req.platformFeePercentage()).append("; ");
            config.setPlatformFeePercentage(req.platformFeePercentage());
        }
        if (req.minPayoutAmount() != null) {
            changes.append("minPayoutAmount: ").append(config.getMinPayoutAmount()).append(" -> ").append(req.minPayoutAmount()).append("; ");
            config.setMinPayoutAmount(req.minPayoutAmount());
        }
        if (req.autoApprovePayoutThreshold() != null) {
            changes.append("autoApprovePayoutThreshold: ").append(config.getAutoApprovePayoutThreshold()).append(" -> ").append(req.autoApprovePayoutThreshold()).append("; ");
            config.setAutoApprovePayoutThreshold(req.autoApprovePayoutThreshold());
        }
        if (req.aiRecommendationsEnabled() != null) {
            changes.append("aiRecommendationsEnabled: ").append(config.isAiRecommendationsEnabled()).append(" -> ").append(req.aiRecommendationsEnabled()).append("; ");
            config.setAiRecommendationsEnabled(req.aiRecommendationsEnabled());
        }
        if (req.aiChatEnabled() != null) {
            changes.append("aiChatEnabled: ").append(config.isAiChatEnabled()).append(" -> ").append(req.aiChatEnabled()).append("; ");
            config.setAiChatEnabled(req.aiChatEnabled());
        }
        if (req.aiFraudDetectionEnabled() != null) {
            changes.append("aiFraudDetectionEnabled: ").append(config.isAiFraudDetectionEnabled()).append(" -> ").append(req.aiFraudDetectionEnabled()).append("; ");
            config.setAiFraudDetectionEnabled(req.aiFraudDetectionEnabled());
        }

        config.setUpdatedAt(Instant.now());
        SystemConfig saved = repository.save(config);

        // admin_audit_logs.target_user_id is NOT NULL and has no generic
        // "target type" column yet (planned for a later audit-log
        // generalization pass) — using the admin's own id as the target is a
        // reasonable placeholder for a config-wide (not per-user) action.
        if (changes.length() > 0) {
            auditService.log(adminId, adminId, "SYSTEM_CONFIG_UPDATED", null, null, changes.toString(), ipAddress);
        }

        return SystemConfigResponse.from(saved);
    }
}
