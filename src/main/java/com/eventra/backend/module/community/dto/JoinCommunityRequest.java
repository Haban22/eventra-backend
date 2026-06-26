package com.eventra.backend.module.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinCommunityRequest {
    @NotNull
    private Long userId;
    private String displayName;
    private String avatarUrl;
}
