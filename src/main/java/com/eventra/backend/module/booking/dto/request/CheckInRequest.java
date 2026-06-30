package com.eventra.backend.module.booking.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CheckInRequest(
        @NotNull UUID attendeeId
) {}