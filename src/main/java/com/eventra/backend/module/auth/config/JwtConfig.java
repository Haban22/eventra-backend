package com.eventra.backend.module.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record JwtConfig(String jwtSecret, long jwtAccessExpirySeconds, long jwtRefreshExpirySeconds) {
    public String secret() {
        return jwtSecret;
    }

    public long accessExpirySeconds() {
        return jwtAccessExpirySeconds;
    }

    public long refreshExpirySeconds() {
        return jwtRefreshExpirySeconds;
    }
}
