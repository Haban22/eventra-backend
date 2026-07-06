package com.eventra.backend.module.event.dto.response;

import com.eventra.backend.module.event.entity.Category;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String icon,
        String description
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getIcon(), c.getDescription());
    }
}