package com.eventra.backend.module.event.valueobject;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Capacity {

    private int maximum;
    private int reserved;

    public boolean hasAvailable(int quantity) {
        return (maximum - reserved) >= quantity;
    }

    public void reserve(int quantity) {
        if (!hasAvailable(quantity)) {
            throw new IllegalStateException("Not enough capacity");
        }
        this.reserved += quantity;
    }

    public void release(int quantity) {
        this.reserved = Math.max(0, this.reserved - quantity);
    }

    public int getAvailable() {
        return maximum - reserved;
    }
}