package com.eventra.backend.module.wallet.repository;

import com.eventra.backend.module.wallet.entity.PayoutMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PayoutMethodRepository extends JpaRepository<PayoutMethod, UUID> {
    List<PayoutMethod> findByUserId(UUID userId);
}
