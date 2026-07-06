-- ==========================================
-- PERSONAL CALENDAR — new module (Phase 11)
-- ==========================================

CREATE TABLE personal_events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    date        TIMESTAMPTZ NOT NULL,
    end_date    TIMESTAMPTZ NOT NULL,
    location    VARCHAR(255),
    type        VARCHAR(20) NOT NULL,
    category    VARCHAR(100),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_personal_events_user ON personal_events (user_id);
