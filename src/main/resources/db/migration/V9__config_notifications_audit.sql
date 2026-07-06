-- ==========================================
-- SYSTEM CONFIG (single row, admin-editable at runtime)
-- ==========================================
-- Backs the frontend's admin Settings page (cancellation window, ticket hold
-- timeout, platform fee %, payout thresholds, AI feature flags). Booking,
-- Wallet, and Gamification services read from this instead of hardcoded values.

CREATE TABLE IF NOT EXISTS system_config (
    id                             SMALLINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    cancellation_window_hours      INT           NOT NULL DEFAULT 48,
    ticket_hold_timeout_minutes    INT           NOT NULL DEFAULT 15,
    platform_fee_percentage        NUMERIC(5,2)  NOT NULL DEFAULT 5.00,
    min_payout_amount              NUMERIC(10,2) NOT NULL DEFAULT 100.00,
    auto_approve_payout_threshold  NUMERIC(10,2) NOT NULL DEFAULT 500.00,
    ai_recommendations_enabled     BOOLEAN       NOT NULL DEFAULT TRUE,
    ai_chat_enabled                BOOLEAN       NOT NULL DEFAULT FALSE,
    ai_fraud_detection_enabled     BOOLEAN       NOT NULL DEFAULT FALSE,
    updated_at                     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

INSERT INTO system_config (id) VALUES (1) ON CONFLICT (id) DO NOTHING;

-- ==========================================
-- NOTIFICATIONS — real schema
-- ==========================================
-- V7 created this table with only an `id` column as a placeholder for the
-- (until now unimplemented) notification module. The table has never been
-- written to, so it's safe to add these columns as NOT NULL directly.
-- `type` is a free-form tag (VARCHAR) rather than a Postgres ENUM, mirroring
-- the existing admin_audit_logs.action_type precedent — the frontend already
-- uses ~17 distinct notification type tags and that list is expected to grow
-- as more trigger points are wired up.

ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS type         VARCHAR(50) NOT NULL,
    ADD COLUMN IF NOT EXISTS title        VARCHAR(200) NOT NULL,
    ADD COLUMN IF NOT EXISTS message      TEXT NOT NULL,
    ADD COLUMN IF NOT EXISTS action_url   TEXT,
    ADD COLUMN IF NOT EXISTS is_read      BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS ai_generated BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS created_at   TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE INDEX IF NOT EXISTS idx_notifications_user        ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, is_read);

-- ==========================================
-- ADMIN AUDIT LOG — generalize beyond user-targeted actions
-- ==========================================
-- admin_audit_logs (V2) was scoped to admin-on-user actions only
-- (target_user_id NOT NULL). The frontend's AuditLogEntry/AuditAction types
-- already cover event/booking/config/broadcast-targeted actions too
-- (src/pages/admin/AdminAuditLogs.tsx). Generalize: target_user_id becomes
-- optional, and a generic target_type/target_id pair covers any target kind.
-- Existing user-targeted rows are backfilled so old and new rows are
-- queryable uniformly through target_type/target_id.

ALTER TABLE admin_audit_logs
    ALTER COLUMN target_user_id DROP NOT NULL,
    ADD COLUMN IF NOT EXISTS target_type VARCHAR(30),
    ADD COLUMN IF NOT EXISTS target_id   VARCHAR(64);

UPDATE admin_audit_logs
SET target_type = 'user', target_id = target_user_id::text
WHERE target_type IS NULL AND target_user_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_aal_target_type ON admin_audit_logs(target_type, target_id);
