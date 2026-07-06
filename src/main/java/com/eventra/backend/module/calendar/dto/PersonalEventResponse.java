package com.eventra.backend.module.calendar.dto;

import com.eventra.backend.module.calendar.entity.PersonalEvent;

import java.time.Instant;
import java.util.UUID;

public record PersonalEventResponse(
        UUID id,
        String title,
        String description,
        Instant date,
        Instant endDate,
        String location,
        String type,
        String category,
        Instant createdAt
) {
    public static PersonalEventResponse from(PersonalEvent e) {
        return new PersonalEventResponse(
                e.getId(), e.getTitle(), e.getDescription(), e.getDate(), e.getEndDate(),
                e.getLocation(), e.getType(), e.getCategory(), e.getCreatedAt()
        );
    }
}
