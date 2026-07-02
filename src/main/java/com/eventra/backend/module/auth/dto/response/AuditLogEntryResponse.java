package com.eventra.backend.module.auth.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogEntryResponse(
        UUID id,
        Instant timestamp,
        UUID adminId,
        String adminEmail,
        String action,
        String targetType,
        String targetId,
        Map<String, Object> previousState,
        Map<String, Object> newState,
        String ipAddress
) {}
