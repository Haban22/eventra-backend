package com.eventra.backend.module.gamification.service;

import com.eventra.backend.common.exception.ResourceNotFoundException;
import com.eventra.backend.common.exception.ValidationException;
import com.eventra.backend.module.gamification.dto.*;
import com.eventra.backend.module.gamification.entity.*;
import com.eventra.backend.module.gamification.enums.GamificationAction;
import com.eventra.backend.module.gamification.enums.TransactionType;
import com.eventra.backend.module.gamification.repository.PointsTransactionRepository;
import com.eventra.backend.module.gamification.repository.RewardsRepository;
import com.eventra.backend.module.gamification.valueobject.Level;
import com.eventra.backend.module.gamification.valueobject.Streak;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardsService {

    private final RewardsRepository rewardsRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final BadgeService badgeService;
    private final LeaderboardService leaderboardService;

    /**
     * Awards XP/points for a gamification action, updates streak, checks badges, and refreshes leaderboard.
     * Idempotent for actions that include a referenceId.
     */
    @Transactional
    public RewardsProfileResponse awardAction(AwardActionRequest req) {
        RewardsProfile profile = getOrCreateProfile(
                req.getUserId(),
                req.getDisplayName() != null ? req.getDisplayName() : "User " + req.getUserId(),
                req.getAvatarUrl());

        GamificationAction action = req.getAction();

        // Idempotency: skip if same action+referenceId already exists (except for chat messages)
        if (req.getReferenceId() != null && action != GamificationAction.SEND_CHAT_MESSAGE) {
            long duplicateCount = pointsTransactionRepository.countByUserIdAndReasonAndReferenceId(
                    req.getUserId(), action.name(), req.getReferenceId());
            if (duplicateCount > 0) {
                return buildProfileResponse(profile);
            }
        }

        LocalDate today = LocalDate.now();
        boolean isNewDay = profile.getLastActivityDate() == null
                || !profile.getLastActivityDate().equals(today);

        // Update streak
        Streak streak = Streak.from(profile.getCurrentStreak(), profile.getLongestStreak(),
                profile.getLastActivityDate());
        Streak updated = streak.update(today);
        profile.setCurrentStreak(updated.getCurrentStreak());
        profile.setLongestStreak(updated.getLongestStreak());
        profile.setLastActivityDate(today);

        // Award primary action XP/points
        long xp = action.getXp();
        long pts = action.getPoints();
        applyXpAndPoints(profile, xp, pts);
        profile = rewardsRepository.save(profile);
        createTransaction(profile, xp, pts, TransactionType.EARNED, action.name(),
                req.getReferenceId(), buildDescription(action));

        // Daily streak bonus (once per day, only for actions that actually earn XP)
        if (isNewDay && xp > 0) {
            long sXp = GamificationAction.DAILY_STREAK.getXp();
            long sPts = GamificationAction.DAILY_STREAK.getPoints();
            applyXpAndPoints(profile, sXp, sPts);
            profile = rewardsRepository.save(profile);
            createTransaction(profile, sXp, sPts, TransactionType.BONUS,
                    "DAILY_STREAK", null, "Daily activity streak bonus");
        }

        // Check badge conditions; award XP bonus for each newly unlocked badge
        List<Badge> newBadges = badgeService.checkAndAwardBadges(profile.getUserId());
        for (Badge badge : newBadges) {
            long bXp = badge.getXpBonus() != null ? badge.getXpBonus() : 0;
            if (bXp > 0) {
                applyXpAndPoints(profile, bXp, 0);
                profile = rewardsRepository.save(profile);
                createTransaction(profile, bXp, 0, TransactionType.BONUS,
                        "BADGE_BONUS", badge.getId().toString(),
                        "Badge unlocked: " + badge.getName());
            }
        }

        // Refresh cached ALL_TIME leaderboard entry
        leaderboardService.refreshUserLeaderboard(profile.getUserId());

        return buildProfileResponse(profile);
    }

    /** Deducts points from the user's balance (e.g. reward redemption). */
    @Transactional
    public PointsTransactionResponse redeemPoints(Long userId, long cost, String description) {
        if (cost <= 0) throw new ValidationException("Redemption cost must be positive");

        RewardsProfile profile = rewardsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Rewards profile not found for user: " + userId));

        if (profile.getPointsBalance() < cost) {
            throw new ValidationException("Insufficient points. Balance: "
                    + profile.getPointsBalance() + ", required: " + cost);
        }
        profile.setPointsBalance(profile.getPointsBalance() - cost);
        profile.setTotalPointsRedeemed(profile.getTotalPointsRedeemed() + cost);
        profile = rewardsRepository.save(profile);

        PointsTransaction tx = createTransaction(profile, 0L, -cost, TransactionType.SPENT,
                "REDEEM_POINTS", null, description != null ? description : "Points redeemed");
        return toTransactionResponse(tx);
    }

    public RewardsProfileResponse getProfile(Long userId) {
        RewardsProfile profile = rewardsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Rewards profile not found for user: " + userId));
        return buildProfileResponse(profile);
    }

    public List<PointsTransactionResponse> getTransactionHistory(Long userId, int page, int size) {
        rewardsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Rewards profile not found for user: " + userId));
        return pointsTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    public RewardsProfile getOrCreateProfile(Long userId, String displayName, String avatarUrl) {
        return rewardsRepository.findByUserId(userId).orElseGet(() -> {
            RewardsProfile p = new RewardsProfile();
            p.setUserId(userId);
            p.setDisplayName(displayName != null ? displayName : "User " + userId);
            p.setAvatarUrl(avatarUrl);
            return rewardsRepository.save(p);
        });
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void applyXpAndPoints(RewardsProfile profile, long xp, long pts) {
        profile.setTotalXP(profile.getTotalXP() + xp);
        if (pts > 0) {
            profile.setPointsBalance(profile.getPointsBalance() + pts);
            profile.setTotalPointsEarned(profile.getTotalPointsEarned() + pts);
        }
        profile.setCurrentLevel(Level.calculateLevel(profile.getTotalXP()));
    }

    private PointsTransaction createTransaction(RewardsProfile profile, long xp, long pts,
                                                TransactionType type, String reason,
                                                String referenceId, String description) {
        PointsTransaction tx = new PointsTransaction();
        tx.setUserId(profile.getUserId());
        tx.setXpAmount(xp);
        tx.setPointsAmount(pts);
        tx.setType(type);
        tx.setReason(reason);
        tx.setReferenceId(referenceId);
        tx.setDescription(description);
        tx.setPointsBalanceAfter(profile.getPointsBalance());
        tx.setXpBalanceAfter(profile.getTotalXP());
        return pointsTransactionRepository.save(tx);
    }

    private RewardsProfileResponse buildProfileResponse(RewardsProfile profile) {
        List<UserBadge> earnedUBs = badgeService.getUserBadgeEntities(profile.getUserId());
        Map<Long, LocalDateTime> earnedMap = new HashMap<>();
        earnedUBs.forEach(ub -> earnedMap.put(ub.getBadge().getId(), ub.getUnlockedAt()));

        Set<Long> earnedIds = earnedMap.keySet();
        List<Badge> allBadges = badgeService.getAllBadgeEntities();

        List<BadgeResponse> earned = allBadges.stream()
                .filter(b -> earnedIds.contains(b.getId()))
                .map(b -> badgeService.toBadgeResponse(b, earnedMap))
                .collect(Collectors.toList());

        List<BadgeResponse> locked = allBadges.stream()
                .filter(b -> !earnedIds.contains(b.getId()))
                .map(b -> badgeService.toBadgeResponse(b, Collections.emptyMap()))
                .collect(Collectors.toList());

        List<PointsTransactionResponse> recent = pointsTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(profile.getUserId(), PageRequest.of(0, 10))
                .stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());

        RewardsProfileResponse res = new RewardsProfileResponse();
        res.setUserId(profile.getUserId());
        res.setDisplayName(profile.getDisplayName());
        res.setAvatarUrl(profile.getAvatarUrl());
        res.setPointsBalance(profile.getPointsBalance());
        res.setTotalXP(profile.getTotalXP());
        res.setTotalPointsEarned(profile.getTotalPointsEarned());
        res.setTotalPointsRedeemed(profile.getTotalPointsRedeemed());
        res.setLevel(Level.from(profile.getTotalXP()));
        res.setCurrentStreak(profile.getCurrentStreak());
        res.setLongestStreak(profile.getLongestStreak());
        res.setLastActivityDate(profile.getLastActivityDate());
        res.setEarnedBadges(earned);
        res.setLockedBadges(locked);
        res.setRecentTransactions(recent);
        return res;
    }

    private PointsTransactionResponse toTransactionResponse(PointsTransaction tx) {
        PointsTransactionResponse r = new PointsTransactionResponse();
        r.setId(tx.getId());
        r.setUserId(tx.getUserId());
        r.setXpAmount(tx.getXpAmount());
        r.setPointsAmount(tx.getPointsAmount());
        r.setType(tx.getType());
        r.setReason(tx.getReason());
        r.setReferenceId(tx.getReferenceId());
        r.setDescription(tx.getDescription());
        r.setPointsBalanceAfter(tx.getPointsBalanceAfter());
        r.setXpBalanceAfter(tx.getXpBalanceAfter());
        r.setCreatedAt(tx.getCreatedAt());
        return r;
    }

    private String buildDescription(GamificationAction action) {
        return switch (action) {
            case RSVP_EVENT        -> "RSVP'd to an event";
            case RSVP_EVENT_EARLY  -> "Early Bird RSVP (within 1 hour of event publish)";
            case ATTEND_EVENT      -> "Attended an event";
            case CHECK_IN          -> "Checked in at event";
            case JOIN_DISCUSSION   -> "Joined a discussion";
            case BOOKMARK_EVENT    -> "Bookmarked an event";
            case SIGNUP_BONUS      -> "Welcome bonus for joining Eventra";
            case DAILY_STREAK      -> "Daily activity streak bonus";
            case SHARE_EVENT       -> "Shared an event";
            case SEND_CHAT_MESSAGE -> "Sent a chat message";
            case LEAVE_REVIEW      -> "Left an event review";
            case REFERRAL          -> "Referred a new user";
            case PROFILE_COMPLETED -> "Completed your profile";
            case BADGE_BONUS       -> "Badge XP bonus";
        };
    }
}
