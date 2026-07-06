package com.eventra.backend.module.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class JoinCommunityRequest {
    @NotNull
    private UUID userId;
    private String displayName;
    private String avatarUrl;
}
