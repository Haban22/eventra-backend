package com.eventra.backend.module.gamification.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Points {
    private final long amount;

    public Points(long amount) {
        this.amount = Math.max(0, amount);
    }

    public Points add(long delta) {
        return new Points(this.amount + delta);
    }

    public Points subtract(long delta) {
        return new Points(Math.max(0, this.amount - delta));
    }

    public boolean canAfford(long cost) {
        return this.amount >= cost;
    }

    public static Points of(long amount) {
        return new Points(amount);
    }

    public static Points zero() {
        return new Points(0);
    }

    @Override
    public String toString() {
        return String.valueOf(amount);
    }
}
