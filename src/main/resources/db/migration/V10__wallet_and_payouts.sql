-- ==========================================
-- WALLET & PAYOUTS — new module (Phase 9)
-- ==========================================
-- Simulated ledger, no real payment processor — mirrors the existing
-- StripeGateway mock pattern (processPayment/refund always "succeed").

CREATE TYPE wallet_status AS ENUM ('ACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION');
CREATE TYPE wallet_transaction_type AS ENUM ('DEPOSIT', 'WITHDRAWAL', 'PAYMENT', 'REFUND', 'PAYOUT', 'FEE', 'EARNING');
CREATE TYPE payout_method_type AS ENUM ('BANK_TRANSFER', 'VODAFONE_CASH', 'INSTAPAY');
CREATE TYPE payout_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'PROCESSING', 'COMPLETED');

CREATE TABLE wallets (
    user_id    UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    balance    NUMERIC(12,2) NOT NULL DEFAULT 0,
    currency   VARCHAR(3)    NOT NULL DEFAULT 'EGP',
    status     wallet_status NOT NULL DEFAULT 'ACTIVE',
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE wallet_transactions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type          wallet_transaction_type NOT NULL,
    amount        NUMERIC(12,2) NOT NULL,
    balance_after NUMERIC(12,2) NOT NULL,
    description   TEXT NOT NULL,
    reference_id  VARCHAR(100),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_wallet_tx_user ON wallet_transactions (user_id);

CREATE TABLE payout_methods (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type           payout_method_type NOT NULL,
    account_name   VARCHAR(200) NOT NULL,
    account_number VARCHAR(100) NOT NULL,
    bank_name      VARCHAR(200),
    phone          VARCHAR(30),
    is_default     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_payout_methods_user ON payout_methods (user_id);

CREATE TABLE payout_requests (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organizer_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount         NUMERIC(12,2) NOT NULL,
    method_id      UUID NOT NULL REFERENCES payout_methods(id),
    status         payout_status NOT NULL DEFAULT 'PENDING',
    notes          TEXT,
    admin_notes    TEXT,
    requested_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at   TIMESTAMPTZ
);
CREATE INDEX idx_payout_requests_organizer ON payout_requests (organizer_id);
CREATE INDEX idx_payout_requests_status ON payout_requests (status);

-- Add WALLET as a real payment method for checkout
ALTER TYPE payment_method ADD VALUE IF NOT EXISTS 'WALLET';
