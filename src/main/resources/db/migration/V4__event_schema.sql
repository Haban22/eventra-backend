-- Enums
CREATE TYPE event_status AS ENUM (
    'DRAFT',
    'PENDING_APPROVAL',
    'PUBLISHED',
    'CANCELLED'
);

CREATE TYPE approval_status AS ENUM (
    'APPROVED',
    'REJECTED',
    'CHANGES_REQUESTED'
);

-- Categories
CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    icon        VARCHAR(50),
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Venues
CREATE TABLE venues (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(150) NOT NULL,
    address       TEXT,
    city          VARCHAR(100),
    latitude      DOUBLE PRECISION,
    longitude     DOUBLE PRECISION,
    max_capacity  INT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE venue_amenities (
    venue_id UUID NOT NULL REFERENCES venues(id) ON DELETE CASCADE,
    amenity  VARCHAR(50) NOT NULL,
    PRIMARY KEY (venue_id, amenity)
);

-- Events
CREATE TABLE events (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organizer_id        UUID NOT NULL REFERENCES users(id),
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    event_datetime      TIMESTAMPTZ NOT NULL,
    location_address    TEXT,
    location_city       VARCHAR(100),
    location_latitude   DOUBLE PRECISION,
    location_longitude  DOUBLE PRECISION,
    venue_id            UUID REFERENCES venues(id),
    category_id         UUID NOT NULL REFERENCES categories(id),
    capacity_maximum    INT NOT NULL DEFAULT 0,
    capacity_reserved   INT NOT NULL DEFAULT 0,
    status              event_status NOT NULL DEFAULT 'DRAFT',
    cover_image_url     TEXT,
    is_online           BOOLEAN NOT NULL DEFAULT FALSE,
    online_url          TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_events_organizer    ON events(organizer_id);
CREATE INDEX idx_events_status       ON events(status);
CREATE INDEX idx_events_category     ON events(category_id);
CREATE INDEX idx_events_datetime     ON events(event_datetime);

-- Event Approvals
CREATE TABLE event_approvals (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id    UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    admin_id    UUID NOT NULL REFERENCES users(id),
    status      approval_status NOT NULL,
    feedback    TEXT,
    reviewed_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE event_tags (
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    tag      VARCHAR(50) NOT NULL,
    PRIMARY KEY (event_id, tag)
);

CREATE TABLE bookmarks (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id   UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_bookmark_user_event UNIQUE (user_id, event_id)
);

CREATE INDEX idx_bookmarks_user ON bookmarks(user_id);

CREATE INDEX idx_approvals_event ON event_approvals(event_id);