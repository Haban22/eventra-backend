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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private static final UUID TEST_USER_ID = UUID.fromString("d3b07384-d113-4956-9d8e-1282ec4567e9");
    private static final UUID OTHER_USER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID UNKNOWN_USER_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    @BeforeEach
    void setUp() {
        profile = newProfile(TEST_USER_ID, "Alice");
        when(rewardsRepository.save(any(RewardsProfile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pointsTransactionRepository.save(any(PointsTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(badgeService.checkAndAwardBadges(any(UUID.class))).thenReturn(Collections.emptyList());
        when(badgeService.getUserBadgeEntities(any(UUID.class))).thenReturn(Collections.emptyList());
        when(badgeService.getAllBadgeEntities()).thenReturn(Collections.emptyList());
        when(pointsTransactionRepository.findByUserIdOrderByCreatedAtDesc(any(UUID.class), any())).thenReturn(Collections.emptyList());
    }

    // ─── awardAction ──────────────────────────────────────────────────────────

    @Test
    void awardAction_createsNewProfileAndAwardsXp() {
        when(rewardsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());
        when(pointsTransactionRepository.countByUserIdAndReasonAndReferenceId(any(), any(), any())).thenReturn(0L);

        AwardActionRequest req = new AwardActionRequest();
        req.setUserId(TEST_USER_ID);
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
        when(rewardsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(profile));
        when(pointsTransactionRepository.countByUserIdAndReasonAndReferenceId(TEST_USER_ID, "RSVP_EVENT", "event-42")).thenReturn(1L);

        AwardActionRequest req = new AwardActionRequest();
        req.setUserId(TEST_USER_ID);
        req.setAction(GamificationAction.RSVP_EVENT);
        req.setReferenceId("event-42");

        rewardsService.awardAction(req);

        // No transaction should be created for a duplicate
        verify(pointsTransactionRepository, never()).save(any());
    }

    @Test
    void awardAction_awardsCorrectXpForRsvp() {
        profile.setLastActivityDate(java.time.LocalDate.of(2020, 1, 1)); // old date → "new day" triggers streak bonus
        when(rewardsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(profile));
        when(pointsTransactionRepository.countByUserIdAndReasonAndReferenceId(any(), any(), any())).thenReturn(0L);

        AwardActionRequest req = new AwardActionRequest();
        req.setUserId(TEST_USER_ID);
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
        when(rewardsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(profile));
        when(pointsTransactionRepository.countByUserIdAndReasonAndReferenceId(any(), any(), any())).thenReturn(0L);

        AwardActionRequest req = new AwardActionRequest();
        req.setUserId(TEST_USER_ID);
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
        when(rewardsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(profile));

        PointsTransactionResponse res = rewardsService.redeemPoints(TEST_USER_ID, 50L, "Free ticket");

        assertEquals(150L, profile.getPointsBalance());
        assertEquals(50L, profile.getTotalPointsRedeemed());
        verify(pointsTransactionRepository).save(any(PointsTransaction.class));
    }

    @Test
    void redeemPoints_throwsValidationExceptionWhenInsufficientBalance() {
        profile.setPointsBalance(30L);
        when(rewardsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(profile));

        assertThrows(ValidationException.class,
                () -> rewardsService.redeemPoints(TEST_USER_ID, 50L, "Too expensive"));
    }

    @Test
    void redeemPoints_throwsValidationExceptionWhenCostIsZero() {
        assertThrows(ValidationException.class,
                () -> rewardsService.redeemPoints(TEST_USER_ID, 0L, "Free?"));
    }

    // ─── getProfile ───────────────────────────────────────────────────────────

    @Test
    void getProfile_returnsResponseForExistingUser() {
        when(rewardsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(profile));

        RewardsProfileResponse res = rewardsService.getProfile(TEST_USER_ID);

        assertNotNull(res);
        assertEquals(TEST_USER_ID, res.getUserId());
        assertEquals("Alice", res.getDisplayName());
    }

    @Test
    void getProfile_throwsResourceNotFoundForUnknownUser() {
        when(rewardsRepository.findByUserId(UNKNOWN_USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rewardsService.getProfile(UNKNOWN_USER_ID));
    }

    // ─── getOrCreateProfile ───────────────────────────────────────────────────

    @Test
    void getOrCreateProfile_createsProfileWhenAbsent() {
        when(rewardsRepository.findByUserId(OTHER_USER_ID)).thenReturn(Optional.empty());

        RewardsProfile result = rewardsService.getOrCreateProfile(OTHER_USER_ID, "Bob", null);

        assertEquals(OTHER_USER_ID, result.getUserId());
        assertEquals("Bob", result.getDisplayName());
        verify(rewardsRepository).save(any(RewardsProfile.class));
    }

    @Test
    void getOrCreateProfile_returnsExistingProfile() {
        when(rewardsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(profile));

        RewardsProfile result = rewardsService.getOrCreateProfile(TEST_USER_ID, "Other Name", null);

        assertEquals(profile, result);
        verify(rewardsRepository, never()).save(any());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static RewardsProfile newProfile(UUID userId, String displayName) {
        RewardsProfile p = new RewardsProfile();
        p.setId(1L);
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
