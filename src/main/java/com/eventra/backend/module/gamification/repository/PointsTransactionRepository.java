package com.eventra.backend.module.gamification.repository;

import com.eventra.backend.module.gamification.entity.PointsTransaction;
import com.eventra.backend.module.gamification.enums.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {
    List<PointsTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<PointsTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndReason(UUID userId, String reason);
    boolean existsByUserIdAndReason(UUID userId, String reason);

    @Query("SELECT t.userId, SUM(t.xpAmount) FROM PointsTransaction t " +
           "WHERE t.createdAt >= :since AND t.type = :type " +
           "GROUP BY t.userId ORDER BY SUM(t.xpAmount) DESC")
    List<Object[]> aggregateXpByUserSince(
            @Param("since") LocalDateTime since,
            @Param("type") TransactionType type,
            Pageable pageable);

    @Query("SELECT COUNT(t) FROM PointsTransaction t " +
           "WHERE t.userId = :userId AND t.reason = :reason AND t.referenceId IS NOT NULL " +
           "AND t.referenceId = :referenceId")
    long countByUserIdAndReasonAndReferenceId(
            @Param("userId") UUID userId,
            @Param("reason") String reason,
            @Param("referenceId") String referenceId);
}
