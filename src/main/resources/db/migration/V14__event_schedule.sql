-- ==========================================
-- EVENT SCHEDULE MODULE — (Phase 14)
-- ==========================================

CREATE TABLE event_schedule_items (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id           UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    title              VARCHAR(200) NOT NULL,
    description        TEXT,
    speaker_name       VARCHAR(100),
    speaker_avatar_url TEXT,
    type               VARCHAR(30) NOT NULL, -- Matches ScheduleItemType enum names
    start_time         TIMESTAMPTZ NOT NULL,
    end_time           TIMESTAMPTZ NOT NULL,
    location           VARCHAR(255), -- room / stage / online room name
    order_index        INT NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_schedule_times CHECK (start_time <= end_time)
);

CREATE INDEX idx_schedule_items_event ON event_schedule_items(event_id);
CREATE INDEX idx_schedule_items_order ON event_schedule_items(order_index);


