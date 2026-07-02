package com.eventra.backend.module.analytics.controller;

import com.eventra.backend.module.analytics.dto.AdminAnalyticsOverviewResponse;
import com.eventra.backend.module.analytics.dto.AdminAnalyticsTrendResponse;
import com.eventra.backend.module.analytics.service.AnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public AdminAnalyticsOverviewResponse getOverview() {
        return analyticsService.getOverview();
    }

    @GetMapping("/trend")
    public AdminAnalyticsTrendResponse getTrend(@RequestParam(defaultValue = "30") int days) {
        return analyticsService.getTrend(days);
    }
}
