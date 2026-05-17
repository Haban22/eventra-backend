package com.eventra.backend.module.auth.repository;

import com.eventra.auth.entity.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthProviderRepository extends JpaRepository<AuthProvider, UUID> {
    Optional<AuthProvider> findByProviderAndProviderUserId(String provider, String providerUserId);
    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);
}
