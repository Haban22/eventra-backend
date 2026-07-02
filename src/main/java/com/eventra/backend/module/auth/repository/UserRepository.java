package com.eventra.backend.module.auth.repository;

import com.eventra.backend.module.auth.entity.User;
import com.eventra.backend.module.auth.entity.UserRole;
import com.eventra.backend.module.auth.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);
    Page<User> findByRole(UserRole role, Pageable pageable);
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    long countByRole(UserRole role);
    long countByStatus(UserStatus status);
    java.util.List<User> findByCreatedAtAfter(java.time.Instant after);
}
