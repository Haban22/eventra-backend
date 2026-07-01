package com.eventra.backend.module.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SearchRequest {

    @NotBlank(message = "query must not be blank")
    private String query;

    private int limit = 5;
}
