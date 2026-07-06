package com.eventra.backend.module.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

public record CreatePersonalEventRequest(
        @NotBlank String title,
        String description,
        @NotNull Instant date,
        @NotNull Instant endDate,
        String location,
        @NotBlank @Pattern(regexp = "personal|reminder") String type,
        String category
) {}
