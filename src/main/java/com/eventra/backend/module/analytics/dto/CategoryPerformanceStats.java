package com.eventra.backend.module.analytics.dto;

public record CategoryPerformanceStats(
        long eventsCount,
        long attendeesCount
) {}
