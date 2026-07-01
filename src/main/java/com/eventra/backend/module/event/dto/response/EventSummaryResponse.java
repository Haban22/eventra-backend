package com.eventra.backend.module.event.dto.response;

import com.eventra.backend.module.event.entity.Event;

import java.time.Instant;
import java.util.UUID;

public record EventSummaryResponse(
        UUID id,
        UUID organizerId,
        String organizerName,
        String organizerAvatarUrl,
        String title,
        String coverImageUrl,
        Instant dateTime,
        String city,
        boolean isOnline,
        CategoryResponse category,
        int capacityAvailable
) {
    public static EventSummaryResponse from(Event e, String organizerName, String organizerAvatarUrl) {
        return new EventSummaryResponse(
                e.getId(),
                e.getOrganizerId(),
                organizerName,
                organizerAvatarUrl,
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