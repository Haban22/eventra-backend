package com.eventra.backend.module.gamification.valueobject;

import lombok.Data;

@Data
public class Level {
    private int currentLevel;
    private long currentXP;
    private long xpForCurrentLevel;
    private long xpToNextLevel;
    private double progressPercentage;

    public static Level from(long totalXP) {
        int level = calculateLevel(totalXP);
        long xpForCurrent = xpThreshold(level);
        long xpForNext = xpThreshold(level + 1);
        long xpIntoLevel = totalXP - xpForCurrent;
        long xpNeeded = xpForNext - xpForCurrent;
        double progress = xpNeeded > 0
                ? Math.min(100.0, (double) xpIntoLevel / xpNeeded * 100.0)
                : 100.0;

        Level l = new Level();
        l.setCurrentLevel(level);
        l.setCurrentXP(totalXP);
        l.setXpForCurrentLevel(xpForCurrent);
        l.setXpToNextLevel(Math.max(0, xpForNext - totalXP));
        l.setProgressPercentage(Math.round(progress * 10.0) / 10.0);
        return l;
    }

    public static int calculateLevel(long totalXP) {
        int level = 1;
        while (totalXP >= xpThreshold(level + 1)) {
            level++;
            if (level > 1000) break;
        }
        return level;
    }

    /**
     * XP required to reach a given level (1-indexed).
     * Level 1=0, Level 2=100, Level 3=250, Level 4=450
     * Formula: xpThreshold(L) = 100*(L-1) + 50*(L-1)*(L-2)/2
     */
    public static long xpThreshold(int level) {
        if (level <= 1) return 0;
        long n = level - 1;
        return 100L * n + 50L * n * (n - 1) / 2;
    }
}
