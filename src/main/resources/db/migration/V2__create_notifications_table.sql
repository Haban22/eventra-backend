-- =============================================================================
-- V2: Notification module tables
-- =============================================================================

-- ---------------------------------------------------------------------------
-- notifications
-- Stores in-app notifications persisted for individual users.
-- ---------------------------------------------------------------------------
CREATE TABLE notifications (
    id         BIGSERIAL    PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    message    TEXT         NOT NULL,
    type       VARCHAR(50)  NOT NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id    BIGINT       NOT NULL,
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Supports paginated list queries scoped to a user (ORDER BY created_at DESC)
CREATE INDEX idx_notifications_user_id         ON notifications (user_id);
-- Supports unread-count and unread-list queries
CREATE INDEX idx_notifications_user_id_is_read ON notifications (user_id, is_read);
-- Supports ORDER BY created_at DESC on the full table
CREATE INDEX idx_notifications_created_at      ON notifications (created_at DESC);

-- ---------------------------------------------------------------------------
-- notification_preferences
-- Per-user preferences: which NotificationChannel to use per NotificationType.
-- One row = one (user, notification_type, channel) triple; defaults to enabled.
-- ---------------------------------------------------------------------------
CREATE TABLE notification_preferences (
    id                BIGSERIAL   PRIMARY KEY,
    user_id           BIGINT      NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    channel           VARCHAR(20) NOT NULL,
    enabled           BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_notification_pref_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_notification_pref_user_type_channel
        UNIQUE (user_id, notification_type, channel)
);

CREATE INDEX idx_notification_pref_user_id ON notification_preferences (user_id);

-- ---------------------------------------------------------------------------
-- notification_templates
-- Reusable title/message templates keyed by NotificationType.
-- Supports {placeholder} variable substitution in application code.
-- ---------------------------------------------------------------------------
CREATE TABLE notification_templates (
    id                BIGSERIAL    PRIMARY KEY,
    notification_type VARCHAR(50)  NOT NULL,
    title_template    VARCHAR(255) NOT NULL,
    message_template  TEXT         NOT NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_notification_template_type UNIQUE (notification_type)
);
