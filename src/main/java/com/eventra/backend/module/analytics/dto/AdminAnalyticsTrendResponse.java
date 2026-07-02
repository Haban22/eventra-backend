package com.eventra.backend.module.analytics.dto;

import java.util.List;

public record AdminAnalyticsTrendResponse(
        List<DailyCountPoint> newUsersByDay,
        List<DailyCountPoint> bookingsByDay
) {}
