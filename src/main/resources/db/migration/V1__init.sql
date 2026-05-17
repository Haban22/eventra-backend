CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE user_role AS ENUM ('ATTENDEE', 'ORGANIZER', 'ADMIN');

CREATE TYPE user_status AS ENUM (
  'PENDING_EMAIL_VERIFICATION',
  'PENDING_ADMIN_APPROVAL',
  'ACTIVE',
  'REJECTED',
  'SUSPENDED',
  'BANNED',
  'DISABLED'
);

CREATE TABLE users (
  id                       UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  full_name                VARCHAR(100) NOT NULL,
  email                    VARCHAR(255) NOT NULL UNIQUE,
  password_hash            VARCHAR(60),
  phone                    VARCHAR(30),
  role                     user_role    NOT NULL DEFAULT 'ATTENDEE',
  status                   user_status  NOT NULL DEFAULT 'PENDING_EMAIL_VERIFICATION',
  email_verified           BOOLEAN      NOT NULL DEFAULT FALSE,
  profile_picture_url      TEXT,
  language_preference      VARCHAR(10)  NOT NULL DEFAULT 'en',
  notification_preferences JSONB        NOT NULL DEFAULT '{}',
  failed_login_attempts    SMALLINT     NOT NULL DEFAULT 0,
  locked_until             TIMESTAMPTZ,
  created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email  ON users (email);
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_role   ON users (role);

CREATE TABLE organizer_profiles (
  id                       UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id                  UUID         NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  organization_name        VARCHAR(200) NOT NULL,
  organization_description TEXT         NOT NULL,
  website_url              TEXT,
  social_link              TEXT,
  approved_by              UUID         REFERENCES users(id),
  approved_at              TIMESTAMPTZ,
  rejection_reason         TEXT,
  created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE email_verification_tokens (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash  VARCHAR(64) NOT NULL UNIQUE,
  expires_at  TIMESTAMPTZ NOT NULL,
  used        BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_evt_user ON email_verification_tokens (user_id);

CREATE TABLE refresh_tokens (
  id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id          UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash       VARCHAR(64) NOT NULL UNIQUE,
  jti              VARCHAR(36) NOT NULL UNIQUE,
  expires_at       TIMESTAMPTZ NOT NULL,
  access_token_exp TIMESTAMPTZ,
  revoked          BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rt_user ON refresh_tokens (user_id);
CREATE INDEX idx_rt_jti  ON refresh_tokens (jti);

CREATE TABLE password_reset_tokens (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash  VARCHAR(64) NOT NULL UNIQUE,
  expires_at  TIMESTAMPTZ NOT NULL,
  used        BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_prt_user ON password_reset_tokens (user_id);

CREATE TABLE auth_providers (
  id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id          UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  provider         VARCHAR(20) NOT NULL,
  provider_user_id VARCHAR(255) NOT NULL,
  provider_email   VARCHAR(255),
  created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (provider, provider_user_id)
);

CREATE INDEX idx_ap_user ON auth_providers (user_id);

CREATE TABLE admin_audit_logs (
  id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  admin_user_id   UUID        NOT NULL REFERENCES users(id),
  target_user_id  UUID        NOT NULL REFERENCES users(id),
  action_type     VARCHAR(50) NOT NULL,
  previous_status user_status,
  new_status      user_status,
  action_reason   TEXT,
  ip_address      INET,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_aal_admin  ON admin_audit_logs (admin_user_id);
CREATE INDEX idx_aal_target ON admin_audit_logs (target_user_id);
