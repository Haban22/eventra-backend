package com.eventra.backend.module.event.dto.request;

import java.time.Instant;
import java.util.UUID;

public record EventSearchRequest(
        UUID categoryId,
        String city,
        String keyword,
        Instant from,
        Instant to
) {}