package com.eventra.backend.module.messaging.dto;

import com.eventra.backend.module.messaging.enums.BroadcastTargetRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SendBroadcastRequest(
        @NotBlank String subject,
        @NotBlank String content,
        @NotNull BroadcastTargetRole targetRole,
        UUID eventId
) {}
