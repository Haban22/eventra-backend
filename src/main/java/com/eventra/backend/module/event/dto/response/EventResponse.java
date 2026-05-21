package com.eventra.backend.module.event.dto.response;

import com.eventra.backend.module.event.entity.Event;
import com.eventra.backend.module.event.enums.EventStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventResponse(
        UUID id,
        UUID organizerId,
        String title,
        String description,
        Instant dateTime,
        String locationAddress,
        String locationCity,
        Double locationLatitude,
        Double locationLongitude,
        UUID venueId,
        CategoryResponse category,
        int capacityMaximum,
        int capacityReserved,
        int capacityAvailable,
        EventStatus status,
        String coverImageUrl,
        boolean isOnline,
        String onlineUrl,
        List<String> tags,
        String rejectionFeedback,
        Instant createdAt,
        Instant updatedAt
) {
    public static EventResponse from(Event e) {
        return new EventResponse(
                e.getId(),
                e.getOrganizerId(),
                e.getTitle(),
                e.getDescription(),
                e.getDateTime(),
                e.getLocation() != null ? e.getLocation().getAddress() : null,
                e.getLocation() != null ? e.getLocation().getCity() : null,
                e.getLocation() != null ? e.getLocation().getLatitude() : null,
                e.getLocation() != null ? e.getLocation().getLongitude() : null,
                e.getVenueId(),
                e.getCategory() != null ? CategoryResponse.from(e.getCategory()) : null,
                e.getCapacity() != null ? e.getCapacity().getMaximum() : 0,
                e.getCapacity() != null ? e.getCapacity().getReserved() : 0,
                e.getCapacity() != null ? e.getCapacity().getAvailable() : 0,
                e.getStatus(),
                e.getCoverImageUrl(),
                e.isOnline(),
                e.getOnlineUrl(),
                e.getTags(),
                null,   // populated separately when needed
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static EventResponse from(Event e, String rejectionFeedback) {
        return new EventResponse(
                e.getId(),
                e.getOrganizerId(),
                e.getTitle(),
                e.getDescription(),
                e.getDateTime(),
                e.getLocation() != null ? e.getLocation().getAddress() : null,
                e.getLocation() != null ? e.getLocation().getCity() : null,
                e.getLocation() != null ? e.getLocation().getLatitude() : null,
                e.getLocation() != null ? e.getLocation().getLongitude() : null,
                e.getVenueId(),
                e.getCategory() != null ? CategoryResponse.from(e.getCategory()) : null,
                e.getCapacity() != null ? e.getCapacity().getMaximum() : 0,
                e.getCapacity() != null ? e.getCapacity().getReserved() : 0,
                e.getCapacity() != null ? e.getCapacity().getAvailable() : 0,
                e.getStatus(),
                e.getCoverImageUrl(),
                e.isOnline(),
                e.getOnlineUrl(),
                e.getTags(),
                rejectionFeedback,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}