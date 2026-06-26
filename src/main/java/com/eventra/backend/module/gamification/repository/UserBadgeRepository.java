package com.eventra.backend.module.gamification.repository;

import com.eventra.backend.module.gamification.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUserId(Long userId);
    boolean existsByUserIdAndBadge_Id(Long userId, Long badgeId);
    long countByUserId(Long userId);

    @Query("SELECT COUNT(ub) FROM UserBadge ub WHERE ub.userId = :userId AND ub.badge.name = :badgeName")
    long countByUserIdAndBadgeName(@Param("userId") Long userId, @Param("badgeName") String badgeName);
}
