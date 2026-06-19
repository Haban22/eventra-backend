-- Users table additions
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS city                VARCHAR(100),
    ADD COLUMN IF NOT EXISTS onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS suspension_reason   TEXT,
    ADD COLUMN IF NOT EXISTS suspended_until     TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS must_reset_password BOOLEAN NOT NULL DEFAULT FALSE;

-- User interests
CREATE TABLE IF NOT EXISTS user_interests (
    user_id  UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    interest VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, interest)
);

-- Organizer profile additions
ALTER TABLE organizer_profiles
    ADD COLUMN IF NOT EXISTS experience        TEXT,
    ADD COLUMN IF NOT EXISTS identity_type     VARCHAR(50),
    ADD COLUMN IF NOT EXISTS team_size         VARCHAR(30),
    ADD COLUMN IF NOT EXISTS tagline           VARCHAR(200),
    ADD COLUMN IF NOT EXISTS brand_color       VARCHAR(20),
    ADD COLUMN IF NOT EXISTS logo_url          TEXT,
    ADD COLUMN IF NOT EXISTS is_verified       BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS email_verified_org BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS phone_verified    BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS id_verified       BOOLEAN NOT NULL DEFAULT FALSE;

-- Organizer event types
CREATE TABLE IF NOT EXISTS organizer_event_types (
    organizer_profile_id UUID        NOT NULL REFERENCES organizer_profiles(id) ON DELETE CASCADE,
    event_type           VARCHAR(50) NOT NULL,
    PRIMARY KEY (organizer_profile_id, event_type)
);