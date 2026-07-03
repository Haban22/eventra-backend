package com.eventra.backend.module.event.dto.response;

import com.eventra.backend.module.event.enums.ScheduleItemType;

import java.time.Instant;
import java.util.UUID;

public record ScheduleItemResponse(
        UUID id,
        UUID eventId,
        String title,
        String description,
        String speakerName,
        String speakerAvatarUrl,
        ScheduleItemType type,
        Instant startTime,
        Instant endTime,
        String location,
        int orderIndex
) {}
