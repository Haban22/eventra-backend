package com.eventra.backend.module.gamification.repository;

import com.eventra.backend.module.gamification.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    Optional<Badge> findByName(String name);
    boolean existsByName(String name);
}
