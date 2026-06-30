package com.eventra.backend.module.gamification;

import com.eventra.backend.module.gamification.valueobject.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevelTest {

    @Test
    void xpThresholds_matchSpecification() {
        // From frontend spec: Level 1=0, Level 2=100, Level 3=250, Level 4=450
        assertEquals(0L,   Level.xpThreshold(1));
        assertEquals(100L, Level.xpThreshold(2));
        assertEquals(250L, Level.xpThreshold(3));
        assertEquals(450L, Level.xpThreshold(4));
        assertEquals(700L, Level.xpThreshold(5));
    }

    @Test
    void calculateLevel_returnsOneForZeroXp() {
        assertEquals(1, Level.calculateLevel(0));
    }

    @Test
    void calculateLevel_staysAtLevelOneBeforeThreshold() {
        assertEquals(1, Level.calculateLevel(99));
    }

    @Test
    void calculateLevel_advancesAtExactThreshold() {
        assertEquals(2, Level.calculateLevel(100));
        assertEquals(3, Level.calculateLevel(250));
        assertEquals(4, Level.calculateLevel(450));
    }

    @Test
    void calculateLevel_staysJustBelowNextLevel() {
        assertEquals(2, Level.calculateLevel(249));
        assertEquals(3, Level.calculateLevel(449));
    }

    @Test
    void levelFrom_level1_atZeroXp() {
        Level level = Level.from(0);
        assertEquals(1, level.getCurrentLevel());
        assertEquals(0L, level.getCurrentXP());
        assertEquals(0L, level.getXpForCurrentLevel());
        assertEquals(100L, level.getXpToNextLevel());
        assertEquals(0.0, level.getProgressPercentage(), 0.01);
    }

    @Test
    void levelFrom_level2_halfwayThrough() {
        // Level 2: 100–249 XP (150 XP range). Halfway = 175 XP.
        Level level = Level.from(175);
        assertEquals(2, level.getCurrentLevel());
        assertEquals(175L, level.getCurrentXP());
        assertEquals(100L, level.getXpForCurrentLevel());
        assertEquals(75L, level.getXpToNextLevel());
        assertEquals(50.0, level.getProgressPercentage(), 0.5);
    }

    @Test
    void levelFrom_atExactLevelThreshold_zeroProgress() {
        Level level = Level.from(250);
        assertEquals(3, level.getCurrentLevel());
        assertEquals(0.0, level.getProgressPercentage(), 0.01);
        assertEquals(200L, level.getXpToNextLevel()); // 450 - 250
    }

    @Test
    void levelFrom_level3_withProgress() {
        Level level = Level.from(350);
        assertEquals(3, level.getCurrentLevel());
        // Into level 3: 350 - 250 = 100 out of 200 needed = 50%
        assertEquals(50.0, level.getProgressPercentage(), 0.5);
        assertEquals(100L, level.getXpToNextLevel());
    }
}
