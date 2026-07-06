package com.eventra.backend.module.gamification.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {
    private int rank;
    private UUID userId;
    private String displayName;
    private String avatarUrl;
    private long score;
    private int level;
}
