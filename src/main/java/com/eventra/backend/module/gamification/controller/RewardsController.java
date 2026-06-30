package com.eventra.backend.module.gamification.controller;

import com.eventra.backend.common.response.ApiResponse;
import com.eventra.backend.module.gamification.dto.*;
import com.eventra.backend.module.gamification.enums.LeaderboardType;
import com.eventra.backend.module.gamification.service.BadgeService;
import com.eventra.backend.module.gamification.service.LeaderboardService;
import com.eventra.backend.module.gamification.service.RewardsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class RewardsController {

    private final RewardsService rewardsService;
    private final BadgeService badgeService;
    private final LeaderboardService leaderboardService;

    /**
     * GET /api/gamification/profile/{userId}
     * Returns the full rewards profile: level, XP, points, streaks, badges, and recent transactions.
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<RewardsProfileResponse>> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(rewardsService.getProfile(userId)));
    }

    /**
     * POST /api/gamification/profile
     * Creates (or returns existing) a rewards profile for a user.
     * Body: CreateProfileRequest (userId, displayName, avatarUrl)
     */
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<RewardsProfileResponse>> createProfile(
            @Valid @RequestBody CreateProfileRequest req) {
        rewardsService.getOrCreateProfile(req.getUserId(), req.getDisplayName(), req.getAvatarUrl());
        return ResponseEntity.ok(ApiResponse.success(rewardsService.getProfile(req.getUserId())));
    }

    /**
     * POST /api/gamification/action
     * Awards XP/points for a gamification action.
     * Body: AwardActionRequest (userId, action, optional referenceId/displayName/avatarUrl)
     */
    @PostMapping("/action")
    public ResponseEntity<ApiResponse<RewardsProfileResponse>> awardAction(
            @RequestBody @Valid AwardActionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(rewardsService.awardAction(request)));
    }

    /**
     * POST /api/gamification/redeem
     * Deducts points from a user's balance.
     * Body: RedeemPointsRequest (userId, cost, description)
     */
    @PostMapping("/redeem")
    public ResponseEntity<ApiResponse<PointsTransactionResponse>> redeemPoints(
            @Valid @RequestBody RedeemPointsRequest req) {
        return ResponseEntity.ok(ApiResponse.success(rewardsService.redeemPoints(req.getUserId(), req.getCost(), req.getDescription())));
    }

    /**
     * GET /api/gamification/transactions/{userId}?page=0&size=20
     * Returns paginated transaction history for a user.
     */
    @GetMapping("/transactions/{userId}")
    public ResponseEntity<ApiResponse<List<PointsTransactionResponse>>> getTransactions(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(rewardsService.getTransactionHistory(userId, page, size)));
    }

    /**
     * GET /api/gamification/badges?userId={userId}
     * Lists all badges with unlock status for the given user (userId is optional).
     */
    @GetMapping("/badges")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getAllBadges(
            @RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(badgeService.getAllBadges(userId)));
    }

    /**
     * GET /api/gamification/badges/user/{userId}
     * Returns only the badges a user has earned.
     */
    @GetMapping("/badges/user/{userId}")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getUserBadges(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(badgeService.getUserBadges(userId)));
    }

    /**
     * GET /api/gamification/leaderboard?type=ALL_TIME&limit=50&userId={userId}
     * Returns a leaderboard. type: ALL_TIME (default), WEEKLY, MONTHLY.
     * userId is optional and returns the calling user's rank in the response.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<LeaderboardResponse>> getLeaderboard(
            @RequestParam(defaultValue = "ALL_TIME") LeaderboardType type,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(leaderboardService.getLeaderboard(type, limit, userId)));
    }
}
