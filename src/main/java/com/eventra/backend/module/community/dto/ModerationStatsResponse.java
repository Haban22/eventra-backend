package com.eventra.backend.module.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ModerationStatsResponse {
    private long pendingFlags;
    private long resolvedToday;
    private long totalReports;
    private List<FlaggedContentResponse> flaggedItems;
}
