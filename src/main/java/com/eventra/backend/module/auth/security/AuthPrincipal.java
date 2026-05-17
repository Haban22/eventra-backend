package com.eventra.backend.module.auth.security;

import com.eventra.backend.module.auth.entity.UserRole;

import java.util.UUID;

public record AuthPrincipal(UUID userId, UserRole role, String jti, long expiresAtEpochSeconds) {
}
