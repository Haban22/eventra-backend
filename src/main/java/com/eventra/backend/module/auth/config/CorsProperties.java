package com.eventra.backend.module.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cors")
public record CorsProperties(String allowedOrigins) {
}
