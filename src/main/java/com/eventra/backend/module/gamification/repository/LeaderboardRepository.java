package com.eventra.backend.module.gamification.repository;

import com.eventra.backend.module.gamification.entity.Leaderboard;
import com.eventra.backend.module.gamification.enums.LeaderboardType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {
    List<Leaderboard> findByTypeOrderByRankPositionAsc(LeaderboardType type);
    Optional<Leaderboard> findByUserIdAndType(Long userId, LeaderboardType type);
}
