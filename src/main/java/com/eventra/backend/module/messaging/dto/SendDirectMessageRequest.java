package com.eventra.backend.module.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendDirectMessageRequest(
        @NotNull UUID receiverId,
        @NotBlank String content
) {}
