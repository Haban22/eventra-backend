package com.eventra.backend.module.event.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventRequest(
        @NotBlank String title,
        String description,
        @NotNull @Future Instant dateTime,
        String locationAddress,
        String locationCity,
        Double locationLatitude,
        Double locationLongitude,
        UUID venueId,
        @NotNull UUID categoryId,
        @NotNull @Min(1) int capacityMaximum,
        boolean isOnline,
        String onlineUrl,
        String coverImageUrl,
        @Size(max = 3) List<String> tags
) {}