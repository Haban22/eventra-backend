package com.eventra.backend.module.analytics.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserRecommendationRequest {

    @NotEmpty(message = "interests must not be empty")
    private List<String> interests;

    private List<Map<String, Object>> interactions = List.of();

    private int limit = 10;
}
