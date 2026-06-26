package com.eventra.backend.module.gamification.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {
    private int rank;
    private Long userId;
    private String displayName;
    private String avatarUrl;
    private long score;
    private int level;
}
