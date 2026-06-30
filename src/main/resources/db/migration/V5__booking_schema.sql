-- Enums
CREATE TYPE booking_status AS ENUM (
    'PENDING_PAYMENT',
    'CONFIRMED',
    'CANCELLED'
);

CREATE TYPE payment_method AS ENUM (
    'CREDIT_CARD',
    'DEBIT_CARD',
    'PAYPAL'
);

CREATE TYPE payment_status AS ENUM (
    'PENDING',
    'COMPLETED',
    'FAILED',
    'REFUNDED'
);

CREATE TYPE refund_status AS ENUM (
    'PENDING',
    'COMPLETED',
    'FAILED'
);

CREATE TYPE ticket_type AS ENUM (
    'GENERAL',
    'VIP',
    'EARLY_BIRD'
);

-- Tickets
CREATE TABLE tickets (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id        UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    ticket_type     ticket_type NOT NULL,
    price_amount    NUMERIC(10,2) NOT NULL DEFAULT 0,
    price_currency  VARCHAR(10) NOT NULL DEFAULT 'EGP',
    total_available INT NOT NULL,
    sold            INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tickets_event ON tickets(event_id);

-- Bookings
CREATE TABLE bookings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attendee_id     UUID NOT NULL REFERENCES users(id),
    event_id        UUID NOT NULL REFERENCES events(id),
    total_amount    NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_currency  VARCHAR(10) NOT NULL DEFAULT 'EGP',
    status          booking_status NOT NULL DEFAULT 'PENDING_PAYMENT',
    checked_in      BOOLEAN NOT NULL DEFAULT FALSE,
    checked_in_at   TIMESTAMPTZ,
    hold_expires_at TIMESTAMPTZ,
    transaction_id  VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_bookings_attendee ON bookings(attendee_id);
CREATE INDEX idx_bookings_event    ON bookings(event_id);
CREATE INDEX idx_bookings_status   ON bookings(status);

-- Booking items (embedded collection)
CREATE TABLE booking_items (
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    ticket_id  UUID NOT NULL REFERENCES tickets(id),
    quantity   INT NOT NULL
);

-- Payments
CREATE TABLE payments (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id     UUID NOT NULL REFERENCES bookings(id),
    amount         NUMERIC(10,2) NOT NULL,
    currency       VARCHAR(10) NOT NULL DEFAULT 'EGP',
    payment_method payment_method NOT NULL,
    status         payment_status NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_booking ON payments(booking_id);

-- Refunds
CREATE TABLE refunds (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id      UUID NOT NULL REFERENCES payments(id),
    booking_id      UUID NOT NULL REFERENCES bookings(id),
    refund_amount   NUMERIC(10,2) NOT NULL,
    refund_currency VARCHAR(10) NOT NULL DEFAULT 'EGP',
    status          refund_status NOT NULL DEFAULT 'PENDING',
    reason          TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);