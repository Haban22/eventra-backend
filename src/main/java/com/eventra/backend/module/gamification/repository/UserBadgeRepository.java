package com.eventra.backend.module.gamification.repository;

import com.eventra.backend.module.gamification.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUserId(UUID userId);
    boolean existsByUserIdAndBadge_Id(UUID userId, Long badgeId);
    long countByUserId(UUID userId);

    @Query("SELECT COUNT(ub) FROM UserBadge ub WHERE ub.userId = :userId AND ub.badge.name = :badgeName")
    long countByUserIdAndBadgeName(@Param("userId") UUID userId, @Param("badgeName") String badgeName);
}
