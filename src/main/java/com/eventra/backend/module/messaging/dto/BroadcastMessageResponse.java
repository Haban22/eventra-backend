package com.eventra.backend.module.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record BroadcastMessageResponse(
        UUID id,
        UUID senderId,
        String senderName,
        String senderRole,
        String targetRole,
        String subject,
        String content,
        Instant createdAt,
        long recipientCount,
        UUID eventId
) {}
