package com.eventra.backend.module.wallet.entity;

import com.eventra.backend.module.wallet.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@Setter
public class Wallet {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private String currency = "EGP";

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "wallet_status")
    private WalletStatus status = WalletStatus.ACTIVE;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
