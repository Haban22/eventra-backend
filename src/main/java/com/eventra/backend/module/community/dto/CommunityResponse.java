package com.eventra.backend.module.community.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityResponse {
    private Long id;
    private String name;
    private String description;
    private String coverImage;
    private String category;
    private Long memberCount;
    private Long eventCount;
    private boolean joined;
    private LocalDateTime createdAt;
}
