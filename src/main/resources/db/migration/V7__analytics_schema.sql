-- ==========================================
-- ANALYTICS MODULE SCHEMA
-- ==========================================
-- Note: the live AI recommendation/search/sentiment features are served by the
-- Eventra AI microservice (see AiServiceClient). These tables back the
-- analytics module's JPA entity stubs reserved for future persistence.

CREATE TABLE IF NOT EXISTS event_analytics (
    id UUID PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS predicted_attendance (
    id UUID PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS user_profiles (
    id UUID PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS recommendations (
    id UUID PRIMARY KEY
);

-- ==========================================
-- NOTIFICATION MODULE SCHEMA
-- ==========================================
-- Same situation: these are JPA entity stubs reserved for future persistence.

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS notification_preferences (
    id UUID PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS notification_templates (
    id UUID PRIMARY KEY
);
