package com.eventra.backend.module.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String frontendUrl,
        String corsAllowedOrigins,
        String mailFrom,
        int bcryptStrength,
        String googleClientId,
        boolean skipEmailVerification
) {
}
