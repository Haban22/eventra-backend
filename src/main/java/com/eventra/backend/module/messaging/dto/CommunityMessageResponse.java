package com.eventra.backend.module.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record CommunityMessageResponse(
        UUID id,
        Long communityId,
        UUID userId,
        String userName,
        String userAvatar,
        String content,
        Instant createdAt
) {}
