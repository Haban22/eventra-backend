package com.eventra.backend.module.community.dto;

import com.eventra.backend.module.community.enums.CommunityRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CommunityMemberResponse {
    private UUID userId;
    private String displayName;
    private String avatarUrl;
    private CommunityRole role;
    private LocalDateTime joinedAt;
}
