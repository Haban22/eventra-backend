package com.eventra.backend.module.analytics.dto;

import java.math.BigDecimal;

public record AdminAnalyticsOverviewResponse(
        long totalUsers,
        long totalAttendees,
        long totalOrganizers,
        long totalAdmins,
        long activeUsers,
        long suspendedUsers,
        long bannedUsers,
        long newUsersLast30Days,
        long totalEvents,
        long publishedEvents,
        long pendingApprovalEvents,
        long draftEvents,
        long confirmedBookings,
        long cancelledBookings,
        BigDecimal totalRevenue,
        long pendingPayoutRequests,
        long approvedPayoutRequests,
        long rejectedPayoutRequests
) {}
