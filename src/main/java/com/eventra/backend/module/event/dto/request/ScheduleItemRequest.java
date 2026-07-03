package com.eventra.backend.module.event.dto.request;

import com.eventra.backend.module.event.enums.ScheduleItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record ScheduleItemRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        @Size(max = 100) String speakerName,
        String speakerAvatarUrl,
        @NotNull ScheduleItemType type,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @Size(max = 255) String location,
        int orderIndex
) {}
