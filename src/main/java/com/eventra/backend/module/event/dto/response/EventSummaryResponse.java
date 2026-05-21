package com.eventra.backend.module.event.dto.response;

import com.eventra.backend.module.event.entity.Event;

import java.time.Instant;
import java.util.UUID;

public record EventSummaryResponse(
        UUID id,
        String title,
        String coverImageUrl,
        Instant dateTime,
        String city,
        boolean isOnline,
        CategoryResponse category,
        int capacityAvailable
) {
    public static EventSummaryResponse from(Event e) {
        return new EventSummaryResponse(
                e.getId(),
                e.getTitle(),
                e.getCoverImageUrl(),
                e.getDateTime(),
                e.getLocation() != null ? e.getLocation().getCity() : null,
                e.isOnline(),
                e.getCategory() != null ? CategoryResponse.from(e.getCategory()) : null,
                e.getCapacity() != null ? e.getCapacity().getAvailable() : 0
        );
    }
}