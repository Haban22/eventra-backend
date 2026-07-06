package com.eventra.backend.module.gamification.dto;

import com.eventra.backend.module.gamification.enums.LeaderboardType;
import com.eventra.backend.module.gamification.valueobject.LeaderboardEntry;
import lombok.Data;
import java.util.List;

@Data
public class LeaderboardResponse {
    private LeaderboardType type;
    private int totalUsers;
    private List<LeaderboardEntry> entries;
    private Integer currentUserRank;
}
