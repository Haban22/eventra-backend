package com.eventra.backend.module.auth.security;

import com.eventra.auth.config.JwtConfig;
import com.eventra.auth.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    private final JwtConfig config;
    private final StringRedisTemplate redisTemplate;
    private final SecretKey key;

    public JwtUtil(JwtConfig config, StringRedisTemplate redisTemplate, Environment environment) {
        this.config = config;
        this.redisTemplate = redisTemplate;

        String secret = config.secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET is not set");
        }

        Set<String> knownPlaceholders = Set.of(
                "dGhpcyBpcyBhIHNlY3JldCBrZXkgZm9yIGV2ZW50cmEgYXV0aCBtb2R1bGUhISE=",
                "changeme",
                "secret"
        );
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (isProd && knownPlaceholders.contains(secret.strip())) {
            throw new IllegalStateException("JWT_SECRET is set to a known placeholder value - set a real secret in production");
        }

        byte[] keyBytes = Base64.getDecoder().decode(secret);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 256 bits (32 bytes) Base64-encoded");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public GeneratedToken generateAccessToken(UUID userId, UserRole role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(config.accessExpirySeconds());
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
        return new GeneratedToken(token, jti, exp);
    }

    public Claims validateAndParse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public String extractJti(String token) {
        return validateAndParse(token).getId();
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey(jti)));
    }

    public void blacklist(String jti, long ttlSeconds) {
        if (jti != null && ttlSeconds > 0) {
            redisTemplate.opsForValue().set(blacklistKey(jti), "1", ttlSeconds, TimeUnit.SECONDS);
        }
    }

    private String blacklistKey(String jti) {
        return "blacklist:jti:" + jti;
    }

    public record GeneratedToken(String token, String jti, Instant expiresAt) {
    }
}
