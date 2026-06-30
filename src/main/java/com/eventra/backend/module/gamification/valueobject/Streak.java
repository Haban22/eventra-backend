package com.eventra.backend.module.gamification.valueobject;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Streak {
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastActivityDate;

    public static Streak from(int current, int longest, LocalDate lastActivity) {
        Streak s = new Streak();
        s.setCurrentStreak(current);
        s.setLongestStreak(longest);
        s.setLastActivityDate(lastActivity);
        return s;
    }

    /**
     * Returns a new Streak after recording activity on 'today'.
     * Consecutive days increment the streak; a gap resets it; same day is a no-op.
     */
    public Streak update(LocalDate today) {
        Streak updated = Streak.from(this.currentStreak, this.longestStreak, this.lastActivityDate);
        if (lastActivityDate == null) {
            updated.setCurrentStreak(1);
            updated.setLongestStreak(Math.max(1, longestStreak));
        } else if (today.equals(lastActivityDate)) {
            // same day – no streak change
        } else if (today.equals(lastActivityDate.plusDays(1))) {
            int newStreak = currentStreak + 1;
            updated.setCurrentStreak(newStreak);
            updated.setLongestStreak(Math.max(newStreak, longestStreak));
        } else {
            // gap – reset
            updated.setCurrentStreak(1);
        }
        updated.setLastActivityDate(today);
        return updated;
    }

    /** True if the streak has been broken (no activity for more than 1 day). */
    public boolean isBroken(LocalDate today) {
        return lastActivityDate != null && today.isAfter(lastActivityDate.plusDays(1));
    }
}
