package com.eventra.backend.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record SuspendUserRequest(
        @NotBlank String reason,
        Instant suspendedUntil
) {}