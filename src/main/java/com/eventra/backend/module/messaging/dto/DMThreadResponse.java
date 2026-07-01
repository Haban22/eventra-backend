package com.eventra.backend.module.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record DMThreadResponse(
        UUID partnerId,
        String partnerName,
        String partnerAvatar,
        String partnerRole,
        String lastMessage,
        Instant lastMessageAt,
        long unreadCount
) {}
