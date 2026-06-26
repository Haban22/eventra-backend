package com.eventra.backend.module.gamification;

import com.eventra.backend.common.exception.ResourceNotFoundException;
import com.eventra.backend.common.exception.ValidationException;
import com.eventra.backend.module.gamification.dto.AwardActionRequest;
import com.eventra.backend.module.gamification.dto.PointsTransactionResponse;
import com.eventra.backend.module.gamification.dto.RewardsProfileResponse;
import com.eventra.backend.module.gamification.entity.PointsTransaction;
import com.eventra.backend.module.gamification.entity.RewardsProfile;
import com.eventra.backend.module.gamification.enums.GamificationAction;
import com.eventra.backend.module.gamification.enums.TransactionType;
import com.eventra.backend.module.gamification.repository.PointsTransactionRepository;
import com.eventra.backend.module.gamification.repository.RewardsRepository;
import com.eventra.backend.module.gamification.service.BadgeService;
import com.eventra.backend.module.gamification.service.LeaderboardService;
import com.eventra.backend.module.gamification.service.RewardsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RewardsServiceTest {

    @Mock private RewardsRepository rewardsRepository;
    @Mock private PointsTransactionRepository pointsTransactionRepository;
    @Mock private BadgeService badgeService;
    @Mock private LeaderboardService leaderboardService;
    @InjectMocks private RewardsService rewardsService;

    private RewardsProfile profile;

    @BeforeEach
    void setUp() {
        profile = newProfile(1L, "Alice");
        when(rewardsRepository.save(any(RewardsProfile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pointsTransactionRepository.save(any(PointsTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(badgeService.checkAndAwardBadges(anyLong())).thenReturn(Collections.emptyList());
        when(badgeService.getUserBadgeEntities(anyLong())).thenReturn(Collections.emptyList());
        when(badgeService.getAllBadgeEntities()).thenReturn(Collections.emptyList());
        when(pointsTransactionRepository.findByUserIdOrderByCreatedAtDesc(anyLong(), any())).thenReturn(Collections.emptyList());
    }

    // ─── awardAction ──────────────────────────────────────────────────────────

    @Test
    void awardAction_createsNewProfileAndAwardsXp() {
        when(rewardsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(pointsTransactionRepository.countByUserIdAndReasonAndReferenceId(any(), any(), any())).thenReturn(0L);

        AwardActionRequest req = new AwardActionRequest();
        req.setUserId(1L);
        req.setAction(GamificationAction.RSVP_EVENT);
        req.setDisplayName("Alice");

        RewardsProfileResponse res = rewardsService.awardAction(req);

        assertNotNull(res);
        // At minimum: primary RSVP (+10 XP) + daily streak bonus (+100 XP) = 110 XP
        assertTrue(res.getTotalXP() >= 10);
        verify(rewardsRepository, atLeastOnce()).save(any(RewardsProfile.class));
        verify(pointsTransactionRepository, atLeastOnce()).save(any(PointsTransaction.class));
    }

    @Test
    void awardAction_skipsDuplicateReferenceId() {
        when(rewardsRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(pointsTransactionRepository.countByUserIdAndReasonAndReferenceId(1L, "RSVP_EVENT", "event-42")).thenReturn(1L);

        AwardActionRequest req = new AwardActionRequest();
        req.setUserId(1L);
        req.setAction(GamificationAction.RSVP_EVENT);
        req.setReferenceId("event-42");

        rewardsService.awardAction(req);

        // No transaction should be created for a duplicate
        verify(pointsTransactionRepository, never()).save(any());
    }

    @Test
    void awardAction_awardsCorrectXpForRsvp() {
        profile.setLastActivityDate(java.time.LocalDate.of(2020, 1, 1)); // old date → "new day" triggers streak bonus
        when(rewardsRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(pointsTransactionRepository.countByUserIdAndReasonAndReferenceId(any(), any(), any())).thenReturn(0L);

        AwardActionRequest req = new AwardActionRequest();
        req.setUserId(1L);
        req.setAction(GamificationAction.RSVP_EVENT);
        req.setReferenceId("event-1");

        RewardsProfileResponse res = rewardsService.awardAction(req);

        // 10 XP (RSVP) + 100 XP (daily streak) = 110
        assertEquals(110L, res.getTotalXP());
        assertEquals(5L + 50L, res.getPointsBalance()); // 5 pts (RSVP) + 50 pts (streak)
    }

    @Test
    void awardAction_doesNotDoubleAwardStreakBonusOnSameDay() {
        profile.setLastActivityDate(java.time.LocalDate.now()); // already active today
        profile.setTotalXP(10L);
        profile.setPointsBalance(5L);
        when(rewardsRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(pointsTransactionRepository.countByUserIdAndReasonAndReferenceId(any(), any(), any())).thenReturn(0L);

        AwardActionRequest req = new AwardActionRequest();
        req.setUserId(1L);
        req.setAction(GamificationAction.RSVP_EVENT);
        req.setReferenceId("event-2");

        RewardsProfileResponse res = rewardsService.awardAction(req);

        // Only 10 XP (RSVP), no streak bonus (same day)
        assertEquals(20L, res.getTotalXP());
    }

    // ─── redeemPoints ─────────────────────────────────────────────────────────

    @Test
    void redeemPoints_deductsBalanceAndCreatesSpentTransaction() {
        profile.setPointsBalance(200L);
        when(rewardsRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        PointsTransactionResponse res = rewardsService.redeemPoints(1L, 50L, "Free ticket");

        assertEquals(150L, profile.getPointsBalance());
        assertEquals(50L, profile.getTotalPointsRedeemed());
        verify(pointsTransactionRepository).save(any(PointsTransaction.class));
    }

    @Test
    void redeemPoints_throwsValidationExceptionWhenInsufficientBalance() {
        profile.setPointsBalance(30L);
        when(rewardsRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        assertThrows(ValidationException.class,
                () -> rewardsService.redeemPoints(1L, 50L, "Too expensive"));
    }

    @Test
    void redeemPoints_throwsValidationExceptionWhenCostIsZero() {
        assertThrows(ValidationException.class,
                () -> rewardsService.redeemPoints(1L, 0L, "Free?"));
    }

    // ─── getProfile ───────────────────────────────────────────────────────────

    @Test
    void getProfile_returnsResponseForExistingUser() {
        when(rewardsRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        RewardsProfileResponse res = rewardsService.getProfile(1L);

        assertNotNull(res);
        assertEquals(1L, res.getUserId());
        assertEquals("Alice", res.getDisplayName());
    }

    @Test
    void getProfile_throwsResourceNotFoundForUnknownUser() {
        when(rewardsRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rewardsService.getProfile(99L));
    }

    // ─── getOrCreateProfile ───────────────────────────────────────────────────

    @Test
    void getOrCreateProfile_createsProfileWhenAbsent() {
        when(rewardsRepository.findByUserId(5L)).thenReturn(Optional.empty());

        RewardsProfile result = rewardsService.getOrCreateProfile(5L, "Bob", null);

        assertEquals(5L, result.getUserId());
        assertEquals("Bob", result.getDisplayName());
        verify(rewardsRepository).save(any(RewardsProfile.class));
    }

    @Test
    void getOrCreateProfile_returnsExistingProfile() {
        when(rewardsRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        RewardsProfile result = rewardsService.getOrCreateProfile(1L, "Other Name", null);

        assertEquals(profile, result);
        verify(rewardsRepository, never()).save(any());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static RewardsProfile newProfile(Long userId, String displayName) {
        RewardsProfile p = new RewardsProfile();
        p.setId(userId);
        p.setUserId(userId);
        p.setDisplayName(displayName);
        p.setPointsBalance(0L);
        p.setTotalXP(0L);
        p.setTotalPointsEarned(0L);
        p.setTotalPointsRedeemed(0L);
        p.setCurrentLevel(1);
        p.setCurrentStreak(0);
        p.setLongestStreak(0);
        return p;
    }
}
