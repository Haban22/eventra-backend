package com.eventra.backend.module.messaging.dto;

import jakarta.validation.constraints.NotBlank;

// Shared by event-chat and community-chat send endpoints — both are just {content}.
public record SendMessageContentRequest(
        @NotBlank String content
) {}
