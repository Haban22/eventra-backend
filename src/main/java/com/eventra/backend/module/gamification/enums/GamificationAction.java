package com.eventra.backend.module.gamification.enums;

public enum GamificationAction {
    RSVP_EVENT(10, 5),
    RSVP_EVENT_EARLY(10, 5),     // same XP/pts as RSVP, also triggers Early Bird badge check
    ATTEND_EVENT(50, 25),
    CHECK_IN(50, 0),
    JOIN_DISCUSSION(15, 5),
    BOOKMARK_EVENT(5, 2),
    SIGNUP_BONUS(50, 0),
    DAILY_STREAK(100, 50),
    SHARE_EVENT(10, 5),
    SEND_CHAT_MESSAGE(5, 0),
    LEAVE_REVIEW(20, 10),
    REFERRAL(200, 100),
    PROFILE_COMPLETED(0, 0),     // no XP/pts – only triggers Verified Attendee badge
    BADGE_BONUS(0, 0);           // XP amount provided at call site from badge.xpBonus

    private final int xp;
    private final int points;

    GamificationAction(int xp, int points) {
        this.xp = xp;
        this.points = points;
    }

    public int getXp() { return xp; }
    public int getPoints() { return points; }
}
