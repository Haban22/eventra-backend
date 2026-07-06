package com.eventra.backend.module.event.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank String name,
        String icon,
        String description
) {}