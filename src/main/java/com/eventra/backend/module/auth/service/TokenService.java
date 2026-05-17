package com.eventra.backend.module.auth.service;

import com.eventra.backend.module.auth.config.JwtConfig;
import com.eventra.backend.module.auth.dto.response.AuthResponse;
import com.eventra.backend.module.auth.dto.response.UserSummaryResponse;
import com.eventra.backend.module.auth.entity.RefreshToken;
import com.eventra.backend.module.auth.entity.User;
import com.eventra.backend.module.auth.repository.RefreshTokenRepository;
import com.eventra.backend.module.auth.security.JwtUtil;
import com.eventra.backend.module.auth.util.SecureTokenGenerator;
import com.eventra.backend.module.auth.util.TokenHashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;

    public TokenService(RefreshTokenRepository refreshTokenRepository, JwtUtil jwtUtil, JwtConfig jwtConfig) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.jwtConfig = jwtConfig;
    }

    @Transactional
    public AuthResponse issue(User user, Boolean created) {
        JwtUtil.GeneratedToken access = jwtUtil.generateAccessToken(user.getId(), user.getRole());
        String rawRefresh = SecureTokenGenerator.generate();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(TokenHashUtil.sha256(rawRefresh));
        refreshToken.setJti(access.jti());
        refreshToken.setExpiresAt(Instant.now().plusSeconds(jwtConfig.refreshExpirySeconds()));
        refreshToken.setAccessTokenExp(access.expiresAt());
        refreshTokenRepository.save(refreshToken);
        return new AuthResponse(access.token(), rawRefresh, "Bearer", jwtConfig.accessExpirySeconds(), UserSummaryResponse.from(user), created);
    }

    public void blacklist(String jti, long ttlSeconds) {
        jwtUtil.blacklist(jti, ttlSeconds);
    }

    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenRepository.revokeByTokenHash(TokenHashUtil.sha256(rawRefreshToken));
        }
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }

    @Transactional
    public void bulkRevokeAndBlacklist(User user) {
        Instant now = Instant.now();
        var activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalseAndExpiresAtAfter(user.getId(), now);
        for (RefreshToken refreshToken : activeTokens) {
            Instant exp = refreshToken.getAccessTokenExp() == null ? now.plusSeconds(jwtConfig.accessExpirySeconds()) : refreshToken.getAccessTokenExp();
            blacklist(refreshToken.getJti(), exp.getEpochSecond() - now.getEpochSecond());
        }
        refreshTokenRepository.revokeAllForUser(user.getId());
    }
}
