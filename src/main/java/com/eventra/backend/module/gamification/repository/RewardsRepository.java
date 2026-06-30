package com.eventra.backend.module.gamification.repository;

import com.eventra.backend.module.gamification.entity.RewardsProfile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RewardsRepository extends JpaRepository<RewardsProfile, Long> {
    Optional<RewardsProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
    List<RewardsProfile> findAllByOrderByTotalXPDesc(Pageable pageable);
    List<RewardsProfile> findByUserIdIn(List<UUID> userIds);
}
