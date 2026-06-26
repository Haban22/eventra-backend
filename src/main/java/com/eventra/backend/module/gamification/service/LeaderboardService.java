package com.eventra.backend.module.gamification.service;

import com.eventra.backend.module.gamification.dto.LeaderboardResponse;
import com.eventra.backend.module.gamification.entity.Leaderboard;
import com.eventra.backend.module.gamification.entity.RewardsProfile;
import com.eventra.backend.module.gamification.enums.LeaderboardType;
import com.eventra.backend.module.gamification.enums.TransactionType;
import com.eventra.backend.module.gamification.repository.LeaderboardRepository;
import com.eventra.backend.module.gamification.repository.PointsTransactionRepository;
import com.eventra.backend.module.gamification.repository.RewardsRepository;
import com.eventra.backend.module.gamification.valueobject.LeaderboardEntry;
import com.eventra.backend.module.gamification.valueobject.Level;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private static final int DEFAULT_LIMIT = 100;

    private final RewardsRepository rewardsRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final LeaderboardRepository leaderboardRepository;

    public LeaderboardResponse getLeaderboard(LeaderboardType type, int limit, Long currentUserId) {
        int effectiveLimit = limit > 0 ? Math.min(limit, 500) : DEFAULT_LIMIT;
        List<LeaderboardEntry> entries = computeEntries(type, effectiveLimit);

        LeaderboardResponse response = new LeaderboardResponse();
        response.setType(type);
        response.setTotalUsers((int) rewardsRepository.count());
        response.setEntries(entries);

        if (currentUserId != null) {
            Integer rank = entries.stream()
                    .filter(e -> currentUserId.equals(e.getUserId()))
                    .mapToInt(LeaderboardEntry::getRank)
                    .boxed()
                    .findFirst()
                    .orElse(null);
            response.setCurrentUserRank(rank);
        }
        return response;
    }

    /** Updates the cached ALL_TIME leaderboard row for a given user. */
    public void refreshUserLeaderboard(Long userId) {
        rewardsRepository.findByUserId(userId).ifPresent(profile -> {
            Leaderboard entry = leaderboardRepository.findByUserIdAndType(userId, LeaderboardType.ALL_TIME)
                    .orElseGet(() -> {
                        Leaderboard e = new Leaderboard();
                        e.setUserId(userId);
                        e.setType(LeaderboardType.ALL_TIME);
                        e.setScore(0L);
                        return e;
                    });
            entry.setDisplayName(profile.getDisplayName());
            entry.setScore(profile.getTotalXP());
            entry.setLastUpdated(LocalDateTime.now());
            leaderboardRepository.save(entry);
        });
    }

    // ─── Private ──────────────────────────────────────────────────────────────

    private List<LeaderboardEntry> computeEntries(LeaderboardType type, int limit) {
        return switch (type) {
            case ALL_TIME -> computeAllTime(limit);
            case WEEKLY   -> computePeriod(limit, LocalDateTime.now().minusDays(7));
            case MONTHLY  -> computePeriod(limit, LocalDateTime.now().minusDays(30));
        };
    }

    private List<LeaderboardEntry> computeAllTime(int limit) {
        List<RewardsProfile> profiles = rewardsRepository
                .findAllByOrderByTotalXPDesc(PageRequest.of(0, limit));
        List<LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;
        for (RewardsProfile p : profiles) {
            entries.add(new LeaderboardEntry(
                    rank++, p.getUserId(), p.getDisplayName(), p.getAvatarUrl(),
                    p.getTotalXP(), Level.calculateLevel(p.getTotalXP())
            ));
        }
        return entries;
    }

    private List<LeaderboardEntry> computePeriod(int limit, LocalDateTime since) {
        List<Object[]> rows = pointsTransactionRepository.aggregateXpByUserSince(
                since, TransactionType.EARNED, PageRequest.of(0, limit));

        List<Long> userIds = rows.stream()
                .map(r -> ((Number) r[0]).longValue())
                .collect(Collectors.toList());

        Map<Long, RewardsProfile> profileMap = rewardsRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(RewardsProfile::getUserId, p -> p));

        List<LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            Long uid = ((Number) row[0]).longValue();
            long score = ((Number) row[1]).longValue();
            RewardsProfile p = profileMap.get(uid);
            entries.add(new LeaderboardEntry(
                    rank++, uid,
                    p != null ? p.getDisplayName() : "Unknown",
                    p != null ? p.getAvatarUrl() : null,
                    score,
                    p != null ? Level.calculateLevel(p.getTotalXP()) : 1
            ));
        }
        return entries;
    }
}
