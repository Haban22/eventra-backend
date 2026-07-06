-- ==========================================
-- MESSAGING & CHAT — new module (Phase 10)
-- ==========================================
-- REST persists (source of truth); real-time delivery is a SimpMessagingTemplate
-- push after each write (see WebSocketConfig) — there is no STOMP write path.

CREATE TABLE direct_messages (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content     TEXT NOT NULL,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_dm_sender ON direct_messages (sender_id);
CREATE INDEX idx_dm_receiver ON direct_messages (receiver_id);
CREATE INDEX idx_dm_participants ON direct_messages (sender_id, receiver_id);

CREATE TABLE event_messages (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id   UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content    TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_event_messages_event ON event_messages (event_id);

CREATE TABLE community_messages (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    community_id BIGINT NOT NULL REFERENCES community(id) ON DELETE CASCADE,
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content      TEXT NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_community_messages_community ON community_messages (community_id);

CREATE TYPE broadcast_target_role AS ENUM ('ATTENDEE', 'ORGANIZER');

CREATE TABLE broadcast_messages (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id    UUID REFERENCES events(id) ON DELETE CASCADE,
    subject     VARCHAR(200) NOT NULL,
    content     TEXT NOT NULL,
    target_role broadcast_target_role NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_broadcast_target_role ON broadcast_messages (target_role);
CREATE INDEX idx_broadcast_sender ON broadcast_messages (sender_id);
CREATE INDEX idx_broadcast_event ON broadcast_messages (event_id);
