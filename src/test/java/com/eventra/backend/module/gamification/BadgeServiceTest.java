package com.eventra.backend.module.gamification;

import com.eventra.backend.module.gamification.dto.BadgeResponse;
import com.eventra.backend.module.gamification.entity.Badge;
import com.eventra.backend.module.gamification.entity.RewardsProfile;
import com.eventra.backend.module.gamification.entity.UserBadge;
import com.eventra.backend.module.gamification.enums.BadgeCategory;
import com.eventra.backend.module.gamification.enums.BadgeTier;
import com.eventra.backend.module.gamification.repository.BadgeRepository;
import com.eventra.backend.module.gamification.repository.PointsTransactionRepository;
import com.eventra.backend.module.gamification.repository.RewardsRepository;
import com.eventra.backend.module.gamification.repository.UserBadgeRepository;
import com.eventra.backend.module.gamification.service.BadgeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock private BadgeRepository badgeRepository;
    @Mock private UserBadgeRepository userBadgeRepository;
    @Mock private PointsTransactionRepository pointsTransactionRepository;
    @Mock private RewardsRepository rewardsRepository;
    @InjectMocks private BadgeService badgeService;

    // ─── checkAndAwardBadges ──────────────────────────────────────────────────

    @Test
    void checkAndAwardBadges_awardsFirstAttendeeBadgeOnFirstRsvp() {
        Badge badge = badge(1L, "First Attendee");
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(userBadgeRepository.existsByUserIdAndBadge_Id(1L, 1L)).thenReturn(false);
        when(pointsTransactionRepository.countByUserIdAndReason(1L, "RSVP_EVENT")).thenReturn(1L);
        when(pointsTransactionRepository.countByUserIdAndReason(1L, "RSVP_EVENT_EARLY")).thenReturn(0L);
        when(userBadgeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Badge> awarded = badgeService.checkAndAwardBadges(1L);

        assertEquals(1, awarded.size());
        assertEquals("First Attendee", awarded.get(0).getName());
        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void checkAndAwardBadges_doesNotAwardBadgeAlreadyEarned() {
        Badge badge = badge(1L, "First Attendee");
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(userBadgeRepository.existsByUserIdAndBadge_Id(1L, 1L)).thenReturn(true);

        List<Badge> awarded = badgeService.checkAndAwardBadges(1L);

        assertTrue(awarded.isEmpty());
        verify(userBadgeRepository, never()).save(any());
    }

    @Test
    void checkAndAwardBadges_awardsEventExplorerAfterFiveAttendances() {
        Badge badge = badge(2L, "Event Explorer");
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(userBadgeRepository.existsByUserIdAndBadge_Id(1L, 2L)).thenReturn(false);
        when(pointsTransactionRepository.countByUserIdAndReason(1L, "ATTEND_EVENT")).thenReturn(5L);
        when(userBadgeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Badge> awarded = badgeService.checkAndAwardBadges(1L);

        assertEquals(1, awarded.size());
        assertEquals("Event Explorer", awarded.get(0).getName());
    }

    @Test
    void checkAndAwardBadges_doesNotAwardEventExplorerBeforeFiveAttendances() {
        Badge badge = badge(2L, "Event Explorer");
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(userBadgeRepository.existsByUserIdAndBadge_Id(1L, 2L)).thenReturn(false);
        when(pointsTransactionRepository.countByUserIdAndReason(1L, "ATTEND_EVENT")).thenReturn(4L);

        List<Badge> awarded = badgeService.checkAndAwardBadges(1L);

        assertTrue(awarded.isEmpty());
    }

    @Test
    void checkAndAwardBadges_awardsStreakMasterWhenStreakIsSevenOrMore() {
        Badge badge = badge(3L, "Streak Master");
        RewardsProfile profile = new RewardsProfile();
        profile.setCurrentStreak(7);

        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(userBadgeRepository.existsByUserIdAndBadge_Id(1L, 3L)).thenReturn(false);
        when(rewardsRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(userBadgeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Badge> awarded = badgeService.checkAndAwardBadges(1L);

        assertEquals(1, awarded.size());
        assertEquals("Streak Master", awarded.get(0).getName());
    }

    @Test
    void checkAndAwardBadges_awardsInfluencerAfterFiveShares() {
        Badge badge = badge(4L, "Influencer");
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(userBadgeRepository.existsByUserIdAndBadge_Id(1L, 4L)).thenReturn(false);
        when(pointsTransactionRepository.countByUserIdAndReason(1L, "SHARE_EVENT")).thenReturn(5L);
        when(userBadgeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Badge> awarded = badgeService.checkAndAwardBadges(1L);

        assertEquals(1, awarded.size());
    }

    @Test
    void checkAndAwardBadges_awardsVerifiedAttendeeOnProfileComplete() {
        Badge badge = badge(5L, "Verified Attendee");
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(userBadgeRepository.existsByUserIdAndBadge_Id(1L, 5L)).thenReturn(false);
        when(pointsTransactionRepository.countByUserIdAndReason(1L, "PROFILE_COMPLETED")).thenReturn(1L);
        when(userBadgeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Badge> awarded = badgeService.checkAndAwardBadges(1L);

        assertEquals(1, awarded.size());
        assertEquals("Verified Attendee", awarded.get(0).getName());
    }

    @Test
    void checkAndAwardBadges_returnsEmptyListWhenNoBadgesExist() {
        when(badgeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Badge> awarded = badgeService.checkAndAwardBadges(1L);

        assertTrue(awarded.isEmpty());
    }

    // ─── getAllBadges ──────────────────────────────────────────────────────────

    @Test
    void getAllBadges_marksEarnedBadgesAsUnlocked() {
        Badge badge = badge(1L, "First Attendee");
        UserBadge ub = new UserBadge();
        ub.setUserId(1L);
        ub.setBadge(badge);

        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(userBadgeRepository.findByUserId(1L)).thenReturn(List.of(ub));

        List<BadgeResponse> responses = badgeService.getAllBadges(1L);

        assertEquals(1, responses.size());
        assertTrue(responses.get(0).isUnlocked());
    }

    @Test
    void getAllBadges_marksUnearnedBadgesAsLocked() {
        Badge badge = badge(1L, "First Attendee");

        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(userBadgeRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        List<BadgeResponse> responses = badgeService.getAllBadges(1L);

        assertEquals(1, responses.size());
        assertFalse(responses.get(0).isUnlocked());
    }

    @Test
    void getAllBadges_withNullUserIdReturnsAllAsLocked() {
        Badge badge = badge(1L, "First Attendee");
        when(badgeRepository.findAll()).thenReturn(List.of(badge));

        List<BadgeResponse> responses = badgeService.getAllBadges(null);

        assertEquals(1, responses.size());
        assertFalse(responses.get(0).isUnlocked());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static Badge badge(Long id, String name) {
        Badge b = new Badge();
        b.setId(id);
        b.setName(name);
        b.setDescription("desc");
        b.setCategory(BadgeCategory.ACHIEVEMENT);
        b.setTier(BadgeTier.BRONZE);
        b.setUnlockCondition("condition");
        b.setXpBonus(50);
        return b;
    }
}
