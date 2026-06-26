package com.eventra.backend.module.gamification.entity;

import com.eventra.backend.module.gamification.enums.LeaderboardType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "type"}))
public class Leaderboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaderboardType type;

    @Column(nullable = false)
    private Long score = 0L;

    private Integer rankPosition;

    private LocalDateTime lastUpdated;
}
