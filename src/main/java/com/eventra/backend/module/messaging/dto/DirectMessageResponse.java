package com.eventra.backend.module.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record DirectMessageResponse(
        UUID id,
        UUID senderId,
        String senderName,
        String senderAvatar,
        String senderRole,
        UUID receiverId,
        String receiverName,
        String receiverRole,
        String content,
        Instant createdAt,
        boolean isRead
) {}
