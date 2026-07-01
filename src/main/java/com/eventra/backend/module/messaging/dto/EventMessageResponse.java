package com.eventra.backend.module.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record EventMessageResponse(
        UUID id,
        UUID eventId,
        UUID userId,
        String userName,
        String userAvatar,
        String userRole,
        String content,
        Instant createdAt
) {}
