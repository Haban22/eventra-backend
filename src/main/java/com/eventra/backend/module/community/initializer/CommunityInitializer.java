package com.eventra.backend.module.community.initializer;

import com.eventra.backend.module.community.entity.Community;
import com.eventra.backend.module.community.repository.CommunityRepository;
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
public class CommunityInitializer {

    private final CommunityRepository communityRepository;

    private record CommunityDef(String name, String description, String category,
                                 String coverImage, long memberCount, long eventCount) {}

    private static final List<CommunityDef> SEEDS = List.of(
        new CommunityDef(
            "Cairo Music Lovers",
            "For everyone passionate about Cairo's vibrant music scene — jazz, classical, electronic and everything in between.",
            "Music",
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800",
            12500, 45),
        new CommunityDef(
            "Tech Cairo Hub",
            "Cairo's go-to community for developers, founders, and tech enthusiasts. Share ideas, attend hackathons, and build the future.",
            "Tech",
            "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
            8900, 38),
        new CommunityDef(
            "Cairo Foodies",
            "Celebrating the best of Egyptian and international cuisine. Discover new restaurants, cooking events, and food festivals in Cairo.",
            "Food & Drink",
            "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800",
            6200, 22),
        new CommunityDef(
            "Fitness & Wellness",
            "Your community for a healthier Cairo lifestyle — yoga, running clubs, nutrition talks, and wellness retreats.",
            "Health & Wellness",
            "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800",
            4800, 17),
        new CommunityDef(
            "Art & Culture Enthusiasts",
            "Connecting Cairo's artists, gallery-goers, and culture lovers. Exhibitions, workshops, and creative conversations.",
            "Art",
            "https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b?w=800",
            3500, 29)
    );

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedCommunities() {
        // Seeding of mock/demo communities has been disabled to keep database clean.
    }
}
