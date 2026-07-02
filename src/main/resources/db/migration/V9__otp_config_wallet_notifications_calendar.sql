-- ==========================================
-- OTP CODES TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS otp_codes (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    purpose     VARCHAR(30) NOT NULL,
    code_hash   VARCHAR(64) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    used        BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_otp_user_purpose ON otp_codes (user_id, purpose);

-- ==========================================
-- SYSTEM CONFIG TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS system_config (
    key   VARCHAR(100) PRIMARY KEY,
    value TEXT         NOT NULL
);

INSERT INTO system_config (key, value) VALUES
    ('cancellationWindowHours',       '24'),
    ('ticketHoldTimeoutMinutes',      '15'),
    ('platformFeePercentage',         '5'),
    ('minPayoutAmount',               '50'),
    ('autoApprovePayoutThreshold',    '500'),
    ('aiRecommendationsEnabled',      'true'),
    ('aiChatEnabled',                 'true'),
    ('aiFraudDetectionEnabled',       'false')
ON CONFLICT (key) DO NOTHING;

-- ==========================================
-- WALLET MODULE
-- ==========================================
CREATE TABLE IF NOT EXISTS wallets (
    id        UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id   UUID          NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    balance   NUMERIC(12,2) NOT NULL DEFAULT 0,
    currency  VARCHAR(5)    NOT NULL DEFAULT 'EGP',
    status    VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wallet_transactions (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id     UUID          NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    type          VARCHAR(20)   NOT NULL,
    amount        NUMERIC(12,2) NOT NULL,
    balance_after NUMERIC(12,2) NOT NULL,
    description   TEXT,
    reference_id  UUID,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_wt_wallet ON wallet_transactions (wallet_id);

CREATE TABLE IF NOT EXISTS payout_methods (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type           VARCHAR(30)  NOT NULL,
    account_name   VARCHAR(100) NOT NULL,
    account_number VARCHAR(100) NOT NULL,
    bank_name      VARCHAR(100),
    phone          VARCHAR(30),
    is_default     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_pm_user ON payout_methods (user_id);

CREATE TABLE IF NOT EXISTS payout_requests (
    id         UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    method_id  UUID          REFERENCES payout_methods(id),
    amount     NUMERIC(12,2) NOT NULL,
    status     VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    notes      TEXT,
    admin_note TEXT,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_pr_user   ON payout_requests (user_id);
CREATE INDEX IF NOT EXISTS idx_pr_status ON payout_requests (status);

-- ==========================================
-- NOTIFICATIONS (add columns to existing stub)
-- ==========================================
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS user_id    UUID        REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS type       VARCHAR(50);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS title      VARCHAR(200);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS message    TEXT;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS is_read    BOOLEAN     NOT NULL DEFAULT FALSE;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS data       JSONB;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

CREATE INDEX IF NOT EXISTS idx_notif_user ON notifications (user_id);

-- ==========================================
-- PERSONAL CALENDAR
-- ==========================================
CREATE TABLE IF NOT EXISTS calendar_events (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    date        TIMESTAMPTZ  NOT NULL,
    end_date    TIMESTAMPTZ,
    location    VARCHAR(300),
    type        VARCHAR(50)  NOT NULL DEFAULT 'PERSONAL',
    category    VARCHAR(50),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_ce_user ON calendar_events (user_id);

-- ==========================================
-- DIRECT MESSAGES
-- ==========================================
CREATE TABLE IF NOT EXISTS direct_messages (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id   UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content     TEXT        NOT NULL,
    is_read     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_dm_sender   ON direct_messages (sender_id);
CREATE INDEX IF NOT EXISTS idx_dm_receiver ON direct_messages (receiver_id);

-- ==========================================
-- BROADCASTS
-- ==========================================
CREATE TABLE IF NOT EXISTS broadcasts (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id   UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject     VARCHAR(200) NOT NULL,
    content     TEXT         NOT NULL,
    target_role VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_bc_sender ON broadcasts (sender_id);
