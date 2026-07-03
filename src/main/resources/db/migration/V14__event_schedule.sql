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

-- ==========================================
-- SEED DATA — Default Event Schedules
-- ==========================================

-- 1. AI Workshop Cairo Schedule
INSERT INTO event_schedule_items (event_id, title, description, speaker_name, type, start_time, end_time, location, order_index)
SELECT 
    e.id, 
    'Registration & Badges', 
    'Pick up your developer badges and grab morning coffee.', 
    NULL, 
    'BREAK', 
    e.event_datetime, 
    e.event_datetime + interval '30 minutes', 
    'Main Entrance Lobby', 
    0
FROM events e WHERE e.title = 'AI Workshop Cairo';

INSERT INTO event_schedule_items (event_id, title, description, speaker_name, type, start_time, end_time, location, order_index)
SELECT 
    e.id, 
    'Opening Keynote: Future of AI in Egypt', 
    'An overview of deep learning opportunities in modern industries.', 
    'Ahmed Hassan', 
    'KEYNOTE', 
    e.event_datetime + interval '30 minutes', 
    e.event_datetime + interval '90 minutes', 
    'Main Auditorium Stage A', 
    1
FROM events e WHERE e.title = 'AI Workshop Cairo';

INSERT INTO event_schedule_items (event_id, title, description, speaker_name, type, start_time, end_time, location, order_index)
SELECT 
    e.id, 
    'Hands-on PyTorch & HuggingFace', 
    'Interactive programming session training a neural network.', 
    'Sarah Ahmed', 
    'WORKSHOP', 
    e.event_datetime + interval '90 minutes', 
    e.event_datetime + interval '180 minutes', 
    'Workshop Room 102', 
    2
FROM events e WHERE e.title = 'AI Workshop Cairo';

-- 2. Startup Networking Night Schedule
INSERT INTO event_schedule_items (event_id, title, description, speaker_name, type, start_time, end_time, location, order_index)
SELECT 
    e.id, 
    'Welcome Reception', 
    'Icebreaking session and appetizers.', 
    NULL, 
    'NETWORKING', 
    e.event_datetime, 
    e.event_datetime + interval '60 minutes', 
    'Rooftop Terrace', 
    0
FROM events e WHERE e.title = 'Startup Networking Night';

INSERT INTO event_schedule_items (event_id, title, description, speaker_name, type, start_time, end_time, location, order_index)
SELECT 
    e.id, 
    'Panel: Fundraising & Scaling in MENA', 
    'VC founders and local angel investors share growth strategies.', 
    'Layla Admin', 
    'PANEL', 
    e.event_datetime + interval '60 minutes', 
    e.event_datetime + interval '120 minutes', 
    'Discussion HubStage', 
    1
FROM events e WHERE e.title = 'Startup Networking Night';

-- 3. Cairo Jazz Festival Schedule
INSERT INTO event_schedule_items (event_id, title, description, speaker_name, type, start_time, end_time, location, order_index)
SELECT 
    e.id, 
    'Opening Solo Act', 
    'Melodic saxophone performance.', 
    'Karim Adel', 
    'PERFORMANCE', 
    e.event_datetime, 
    e.event_datetime + interval '45 minutes', 
    'Garden Arena stage', 
    0
FROM events e WHERE e.title = 'Cairo Jazz Festival';

INSERT INTO event_schedule_items (event_id, title, description, speaker_name, type, start_time, end_time, location, order_index)
SELECT 
    e.id, 
    'Main Jazz Ensemble', 
    'Regional brass band collaboration performance.', 
    'The Cairo Quartet', 
    'PERFORMANCE', 
    e.event_datetime + interval '60 minutes', 
    e.event_datetime + interval '180 minutes', 
    'Grand Bibliotheca Stage', 
    1
FROM events e WHERE e.title = 'Cairo Jazz Festival';
