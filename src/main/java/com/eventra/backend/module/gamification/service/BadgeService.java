package com.eventra.backend.module.gamification.service;

import com.eventra.backend.module.gamification.dto.BadgeResponse;
import com.eventra.backend.module.gamification.entity.Badge;
import com.eventra.backend.module.gamification.entity.UserBadge;
import com.eventra.backend.module.gamification.repository.BadgeRepository;
import com.eventra.backend.module.gamification.repository.PointsTransactionRepository;
import com.eventra.backend.module.gamification.repository.RewardsRepository;
import com.eventra.backend.module.gamification.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final RewardsRepository rewardsRepository;

    public List<Badge> getAllBadgeEntities() {
        return badgeRepository.findAll();
    }

    public List<BadgeResponse> getAllBadges(Long userId) {
        Map<Long, LocalDateTime> earnedMap = getEarnedBadgeMap(userId);
        return badgeRepository.findAll().stream()
                .map(b -> toBadgeResponse(b, earnedMap))
                .collect(Collectors.toList());
    }

    public List<UserBadge> getUserBadgeEntities(Long userId) {
        return userBadgeRepository.findByUserId(userId);
    }

    public List<BadgeResponse> getUserBadges(Long userId) {
        Map<Long, LocalDateTime> earnedMap = getEarnedBadgeMap(userId);
        return userBadgeRepository.findByUserId(userId).stream()
                .map(ub -> toBadgeResponse(ub.getBadge(), earnedMap))
                .collect(Collectors.toList());
    }

    /**
     * Checks all badge conditions and awards newly earned badges.
     * Returns the list of newly awarded Badge entities (caller awards the XP bonuses).
     */
    public List<Badge> checkAndAwardBadges(Long userId) {
        List<Badge> allBadges = badgeRepository.findAll();
        List<Badge> newlyAwarded = new ArrayList<>();

        for (Badge badge : allBadges) {
            if (userBadgeRepository.existsByUserIdAndBadge_Id(userId, badge.getId())) {
                continue;
            }
            if (isConditionMet(userId, badge)) {
                UserBadge ub = new UserBadge();
                ub.setUserId(userId);
                ub.setBadge(badge);
                userBadgeRepository.save(ub);
                newlyAwarded.add(badge);
            }
        }
        return newlyAwarded;
    }

    public BadgeResponse toBadgeResponse(Badge badge, Map<Long, LocalDateTime> earnedMap) {
        BadgeResponse r = new BadgeResponse();
        r.setId(badge.getId());
        r.setName(badge.getName());
        r.setDescription(badge.getDescription());
        r.setCategory(badge.getCategory());
        r.setTier(badge.getTier());
        r.setUnlockCondition(badge.getUnlockCondition());
        r.setXpBonus(badge.getXpBonus());
        r.setIconUrl(badge.getIconUrl());
        r.setUnlocked(earnedMap.containsKey(badge.getId()));
        r.setUnlockedAt(earnedMap.get(badge.getId()));
        return r;
    }

    // ─── Private ──────────────────────────────────────────────────────────────

    private boolean isConditionMet(Long userId, Badge badge) {
        return switch (badge.getName()) {
            case "First Attendee" ->
                    pointsTransactionRepository.countByUserIdAndReason(userId, "RSVP_EVENT")
                            + pointsTransactionRepository.countByUserIdAndReason(userId, "RSVP_EVENT_EARLY") >= 1;
            case "Event Explorer" ->
                    pointsTransactionRepository.countByUserIdAndReason(userId, "ATTEND_EVENT") >= 5;
            case "Community Builder" ->
                    pointsTransactionRepository.countByUserIdAndReason(userId, "JOIN_DISCUSSION") >= 3;
            case "Streak Master" ->
                    rewardsRepository.findByUserId(userId)
                            .map(p -> p.getCurrentStreak() >= 7)
                            .orElse(false);
            case "Super Fan" ->
                    pointsTransactionRepository.countByUserIdAndReason(userId, "ATTEND_EVENT") >= 10;
            case "Early Bird" ->
                    pointsTransactionRepository.countByUserIdAndReason(userId, "RSVP_EVENT_EARLY") >= 1;
            case "Influencer" ->
                    pointsTransactionRepository.countByUserIdAndReason(userId, "SHARE_EVENT") >= 5;
            case "Verified Attendee" ->
                    pointsTransactionRepository.countByUserIdAndReason(userId, "PROFILE_COMPLETED") >= 1;
            default -> false;
        };
    }

    private Map<Long, LocalDateTime> getEarnedBadgeMap(Long userId) {
        if (userId == null) return Collections.emptyMap();
        Map<Long, LocalDateTime> map = new HashMap<>();
        userBadgeRepository.findByUserId(userId)
                .forEach(ub -> map.put(ub.getBadge().getId(), ub.getUnlockedAt()));
        return map;
    }
}
