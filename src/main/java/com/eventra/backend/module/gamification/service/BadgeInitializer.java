package com.eventra.backend.module.gamification.service;

import com.eventra.backend.module.gamification.entity.Badge;
import com.eventra.backend.module.gamification.enums.BadgeCategory;
import com.eventra.backend.module.gamification.enums.BadgeTier;
import com.eventra.backend.module.gamification.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BadgeInitializer {

    private final BadgeRepository badgeRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedBadges() {
        List<BadgeDefinition> definitions = List.of(
            new BadgeDefinition("First Attendee",    "RSVP to your first event",                BadgeCategory.ACHIEVEMENT, BadgeTier.BRONZE,   "RSVP to 1 event",                      50,  "🎟️"),
            new BadgeDefinition("Event Explorer",    "Attend 5 events",                         BadgeCategory.ACHIEVEMENT, BadgeTier.SILVER,   "Attend 5 events",                     100,  "🗺️"),
            new BadgeDefinition("Community Builder", "Join 3 discussions",                      BadgeCategory.PARTICIPATION, BadgeTier.BRONZE, "Join 3 discussions",                   75,  "💬"),
            new BadgeDefinition("Streak Master",     "Maintain a 7-day activity streak",        BadgeCategory.ACHIEVEMENT, BadgeTier.GOLD,     "Maintain 7-day streak",               150,  "🔥"),
            new BadgeDefinition("Super Fan",         "Attend 10 events",                        BadgeCategory.MILESTONE,   BadgeTier.PLATINUM, "Attend 10 events",                    200,  "⭐"),
            new BadgeDefinition("Early Bird",        "RSVP within 1 hour of event publish",     BadgeCategory.PARTICIPATION, BadgeTier.BRONZE, "RSVP within 1 hour of event publish",  50,  "🐦"),
            new BadgeDefinition("Influencer",        "Share 5 events",                          BadgeCategory.PARTICIPATION, BadgeTier.SILVER, "Share 5 events",                       75,  "📣"),
            new BadgeDefinition("Verified Attendee", "Complete all profile fields",             BadgeCategory.MILESTONE,   BadgeTier.BRONZE,   "Complete your profile",                25,  "✅")
        );

        int seeded = 0;
        for (BadgeDefinition def : definitions) {
            if (!badgeRepository.existsByName(def.name())) {
                Badge badge = new Badge();
                badge.setName(def.name());
                badge.setDescription(def.description());
                badge.setCategory(def.category());
                badge.setTier(def.tier());
                badge.setUnlockCondition(def.unlockCondition());
                badge.setXpBonus(def.xpBonus());
                badge.setIconUrl(def.iconUrl());
                badgeRepository.save(badge);
                seeded++;
            }
        }
        if (seeded > 0) {
            log.info("Gamification: seeded {} default badges", seeded);
        }
    }

    private record BadgeDefinition(
            String name, String description, BadgeCategory category, BadgeTier tier,
            String unlockCondition, int xpBonus, String iconUrl) {}
}
