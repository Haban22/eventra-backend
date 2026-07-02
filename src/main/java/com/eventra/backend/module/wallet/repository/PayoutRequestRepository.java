package com.eventra.backend.module.wallet.repository;

import com.eventra.backend.module.wallet.entity.PayoutRequest;
import com.eventra.backend.module.wallet.enums.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PayoutRequestRepository extends JpaRepository<PayoutRequest, UUID> {
    Page<PayoutRequest> findByOrganizerIdOrderByRequestedAtDesc(UUID organizerId, Pageable pageable);
    Page<PayoutRequest> findByStatusOrderByRequestedAtDesc(PayoutStatus status, Pageable pageable);
    Page<PayoutRequest> findAllByOrderByRequestedAtDesc(Pageable pageable);
    long countByStatus(PayoutStatus status);
}
