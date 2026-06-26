package com.eventra.backend.module.gamification;

import com.eventra.backend.module.auth.security.JwtUtil;
import com.eventra.backend.module.gamification.controller.RewardsController;
import com.eventra.backend.module.gamification.dto.*;
import com.eventra.backend.module.gamification.enums.GamificationAction;
import com.eventra.backend.module.gamification.enums.LeaderboardType;
import com.eventra.backend.module.gamification.enums.TransactionType;
import com.eventra.backend.module.gamification.service.BadgeService;
import com.eventra.backend.module.gamification.service.LeaderboardService;
import com.eventra.backend.module.gamification.service.RewardsService;
import com.eventra.backend.module.gamification.valueobject.LeaderboardEntry;
import com.eventra.backend.module.gamification.valueobject.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RewardsController.class)
@Import(RewardsControllerTest.TestSecurityConfig.class)
class RewardsControllerTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtUtil jwtUtil;
    @MockBean private RewardsService rewardsService;
    @MockBean private BadgeService badgeService;
    @MockBean private LeaderboardService leaderboardService;

    // ─── GET /api/gamification/profile/{userId} ───────────────────────────────

    @Test
    void getProfile_returns200WithProfileData() throws Exception {
        RewardsProfileResponse profile = sampleProfile(1L);
        when(rewardsService.getProfile(1L)).thenReturn(profile);

        mockMvc.perform(get("/api/gamification/profile/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.displayName").value("Alice"))
                .andExpect(jsonPath("$.data.totalXP").value(350))
                .andExpect(jsonPath("$.data.level.currentLevel").value(3));
    }

    @Test
    void getProfile_returns404WhenUserNotFound() throws Exception {
        when(rewardsService.getProfile(99L))
                .thenThrow(new com.eventra.backend.common.exception.ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/gamification/profile/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── POST /api/gamification/action ────────────────────────────────────────

    @Test
    void awardAction_returns200WithUpdatedProfile() throws Exception {
        AwardActionRequest req = new AwardActionRequest();
        req.setUserId(1L);
        req.setAction(GamificationAction.RSVP_EVENT);
        req.setReferenceId("event-1");

        RewardsProfileResponse profile = sampleProfile(1L);
        when(rewardsService.awardAction(any(AwardActionRequest.class))).thenReturn(profile);

        mockMvc.perform(post("/api/gamification/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1));
    }

    @Test
    void awardAction_returns400WhenUserIdMissing() throws Exception {
        mockMvc.perform(post("/api/gamification/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"RSVP_EVENT\"}"))
                .andExpect(status().isBadRequest());
    }

    // ─── POST /api/gamification/redeem ────────────────────────────────────────

    @Test
    void redeemPoints_returns200WithTransaction() throws Exception {
        PointsTransactionResponse tx = new PointsTransactionResponse();
        tx.setId(1L);
        tx.setUserId(1L);
        tx.setPointsAmount(-50L);
        tx.setType(TransactionType.SPENT);
        tx.setReason("REDEEM_POINTS");

        when(rewardsService.redeemPoints(eq(1L), eq(50L), any())).thenReturn(tx);

        Map<String, Object> body = Map.of("userId", 1, "cost", 50, "description", "Free ticket");
        mockMvc.perform(post("/api/gamification/redeem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pointsAmount").value(-50))
                .andExpect(jsonPath("$.data.reason").value("REDEEM_POINTS"));
    }

    // ─── GET /api/gamification/transactions/{userId} ──────────────────────────

    @Test
    void getTransactions_returns200WithList() throws Exception {
        PointsTransactionResponse tx = new PointsTransactionResponse();
        tx.setId(1L);
        tx.setXpAmount(10L);
        tx.setReason("RSVP_EVENT");

        when(rewardsService.getTransactionHistory(eq(1L), anyInt(), anyInt())).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/gamification/transactions/1?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].reason").value("RSVP_EVENT"))
                .andExpect(jsonPath("$.data[0].xpAmount").value(10));
    }

    // ─── GET /api/gamification/badges ─────────────────────────────────────────

    @Test
    void getAllBadges_returns200WithBadgeList() throws Exception {
        BadgeResponse badge = new BadgeResponse();
        badge.setId(1L);
        badge.setName("First Attendee");
        badge.setUnlocked(false);

        when(badgeService.getAllBadges(any())).thenReturn(List.of(badge));

        mockMvc.perform(get("/api/gamification/badges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("First Attendee"))
                .andExpect(jsonPath("$.data[0].unlocked").value(false));
    }

    @Test
    void getUserBadges_returns200WithEarnedBadges() throws Exception {
        BadgeResponse badge = new BadgeResponse();
        badge.setId(1L);
        badge.setName("First Attendee");
        badge.setUnlocked(true);

        when(badgeService.getUserBadges(1L)).thenReturn(List.of(badge));

        mockMvc.perform(get("/api/gamification/badges/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].unlocked").value(true));
    }

    // ─── GET /api/gamification/leaderboard ────────────────────────────────────

    @Test
    void getLeaderboard_returnsAllTimeByDefault() throws Exception {
        LeaderboardResponse leaderboard = new LeaderboardResponse();
        leaderboard.setType(LeaderboardType.ALL_TIME);
        leaderboard.setTotalUsers(1);
        leaderboard.setEntries(List.of(
                new LeaderboardEntry(1, 1L, "Alice", null, 350L, 3)
        ));

        when(leaderboardService.getLeaderboard(eq(LeaderboardType.ALL_TIME), anyInt(), any()))
                .thenReturn(leaderboard);

        mockMvc.perform(get("/api/gamification/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("ALL_TIME"))
                .andExpect(jsonPath("$.data.totalUsers").value(1))
                .andExpect(jsonPath("$.data.entries[0].rank").value(1))
                .andExpect(jsonPath("$.data.entries[0].displayName").value("Alice"));
    }

    @Test
    void getLeaderboard_returnsWeeklyLeaderboard() throws Exception {
        LeaderboardResponse leaderboard = new LeaderboardResponse();
        leaderboard.setType(LeaderboardType.WEEKLY);
        leaderboard.setTotalUsers(2);
        leaderboard.setEntries(Collections.emptyList());

        when(leaderboardService.getLeaderboard(eq(LeaderboardType.WEEKLY), anyInt(), any()))
                .thenReturn(leaderboard);

        mockMvc.perform(get("/api/gamification/leaderboard?type=WEEKLY&limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("WEEKLY"));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static RewardsProfileResponse sampleProfile(Long userId) {
        RewardsProfileResponse p = new RewardsProfileResponse();
        p.setUserId(userId);
        p.setDisplayName("Alice");
        p.setPointsBalance(100L);
        p.setTotalXP(350L);  // 350 XP = Level 3 (250-449 range)
        p.setTotalPointsEarned(105L);
        p.setTotalPointsRedeemed(5L);
        p.setLevel(Level.from(350L));  // currentLevel = 3
        p.setCurrentStreak(3);
        p.setLongestStreak(10);
        p.setEarnedBadges(Collections.emptyList());
        p.setLockedBadges(Collections.emptyList());
        p.setRecentTransactions(Collections.emptyList());
        return p;
    }
}
