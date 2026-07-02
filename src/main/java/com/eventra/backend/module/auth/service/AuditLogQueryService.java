package com.eventra.backend.module.auth.service;

import com.eventra.backend.module.auth.dto.response.AuditLogEntryResponse;
import com.eventra.backend.module.auth.dto.response.PageResponse;
import com.eventra.backend.module.auth.entity.AdminAuditLog;
import com.eventra.backend.module.auth.entity.User;
import com.eventra.backend.module.auth.repository.AuditLogRepository;
import com.eventra.backend.module.auth.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// Read side of the audit log — AuditService (write side) stays focused on
// persisting entries; this maps them into the frontend's AuditLogEntry shape,
// including the backend-action-string -> frontend-AuditAction vocabulary mapping.
@Service
public class AuditLogQueryService {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogQueryService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogEntryResponse> getAuditLogs(String targetType, int page, int size) {
        var pageable = PageRequest.of(page, size);
        Page<AdminAuditLog> logs = targetType != null
                ? auditLogRepository.findByTargetTypeOrderByCreatedAtDesc(targetType, pageable)
                : auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);

        Set<UUID> adminIds = logs.getContent().stream().map(AdminAuditLog::getAdminUserId).collect(Collectors.toSet());
        Map<UUID, String> emailsByAdminId = userRepository.findAllById(adminIds).stream()
                .collect(Collectors.toMap(User::getId, User::getEmail));

        var data = logs.stream().map(log -> toResponse(log, emailsByAdminId)).toList();
        return new PageResponse<>(data, logs.getTotalElements(), page, size);
    }

    private AuditLogEntryResponse toResponse(AdminAuditLog log, Map<UUID, String> emailsByAdminId) {
        return new AuditLogEntryResponse(
                log.getId(),
                log.getCreatedAt(),
                log.getAdminUserId(),
                emailsByAdminId.getOrDefault(log.getAdminUserId(), "unknown"),
                mapAction(log.getActionType()),
                log.getTargetType(),
                log.getTargetId() != null ? log.getTargetId() : (log.getTargetUserId() != null ? log.getTargetUserId().toString() : null),
                log.getPreviousStatus() != null ? Map.of("status", log.getPreviousStatus().name()) : null,
                buildNewState(log),
                log.getIpAddress() != null ? log.getIpAddress().getHostAddress() : null
        );
    }

    private Map<String, Object> buildNewState(AdminAuditLog log) {
        if (log.getNewStatus() != null) {
            return Map.of("status", log.getNewStatus().name());
        }
        if (log.getActionReason() != null) {
            return Map.of("reason", log.getActionReason());
        }
        return null;
    }

    // Backend action strings are past-tense NOUN_VERB (e.g. ORGANIZER_APPROVED);
    // the frontend's AuditAction vocabulary is present-tense verb_noun
    // (approve_organizer) — this table is the single source of truth for that
    // mapping. Falls back to a lowercased version of the raw string for any
    // action type not yet in the frontend's vocabulary (defensive — keeps the
    // UI rendering something reasonable rather than breaking if a new backend
    // action type is added without updating both sides).
    private String mapAction(String backendAction) {
        return switch (backendAction) {
            case "ORGANIZER_APPROVED" -> "approve_organizer";
            case "ORGANIZER_REJECTED" -> "reject_organizer";
            case "USER_SUSPENDED", "USER_SUSPENDED_DETAILED" -> "suspend_user";
            case "USER_BANNED" -> "ban_user";
            case "USER_REACTIVATED" -> "unsuspend_user";
            case "ORGANIZER_VERIFIED" -> "grant_verified";
            case "FORCE_PASSWORD_RESET" -> "force_password_reset";
            case "USER_DISABLED" -> "disable_user";
            case "SYSTEM_CONFIG_UPDATED" -> "config_change";
            case "EVENT_APPROVED" -> "approve_event";
            case "EVENT_REJECTED" -> "reject_event";
            case "PAYOUT_APPROVED" -> "approve_payout";
            case "PAYOUT_REJECTED" -> "reject_payout";
            case "CONTENT_APPROVED" -> "content_approve";
            case "CONTENT_REMOVED" -> "content_remove";
            case "CONTENT_WARNED" -> "content_warn";
            default -> backendAction.toLowerCase();
        };
    }
}
