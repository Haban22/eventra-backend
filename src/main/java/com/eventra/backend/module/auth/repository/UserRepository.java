package com.eventra.backend.module.auth.repository;

import com.eventra.auth.entity.User;
import com.eventra.auth.entity.UserRole;
import com.eventra.auth.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);
}
