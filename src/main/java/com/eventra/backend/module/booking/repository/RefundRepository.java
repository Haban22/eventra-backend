package com.eventra.backend.module.booking.repository;

import com.eventra.backend.module.booking.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefundRepository extends JpaRepository<Refund, UUID> {

    Optional<Refund> findByPaymentId(UUID paymentId);

    List<Refund> findByBookingId(UUID bookingId);
}