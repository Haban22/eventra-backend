package com.eventra.backend.module.gamification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateProfileRequest {
    @NotNull
    private UUID userId;

    private String displayName;
    
    private String avatarUrl;
}
