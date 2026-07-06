-- ==========================================
-- DEMO DATA SEED
-- ==========================================
-- Seeds the 3 demo accounts the Eventra2 frontend has always shipped with
-- (sarah/ahmed/admin @demo.com, password "demo123" for all three) plus sample
-- categories/venues/events/tickets, so the existing demo experience keeps
-- working once the frontend is wired to this real backend instead of mocks.

-- Categories
INSERT INTO categories (id, name, icon, description) VALUES
    (gen_random_uuid(), 'Technology', 'cpu',       'Tech talks, workshops, and hackathons'),
    (gen_random_uuid(), 'Business',   'briefcase', 'Networking, startups, and entrepreneurship'),
    (gen_random_uuid(), 'Music',      'music',     'Concerts, gigs, and festivals'),
    (gen_random_uuid(), 'Sports',     'trophy',    'Tournaments and fitness events'),
    (gen_random_uuid(), 'Arts',       'palette',   'Exhibitions, theatre, and design'),
    (gen_random_uuid(), 'Gaming',     'gamepad',   'Esports and gaming expos')
ON CONFLICT (name) DO NOTHING;

-- Venues
INSERT INTO venues (id, name, address, city, latitude, longitude, max_capacity)
SELECT gen_random_uuid(), v.name, v.address, v.city, v.lat, v.lng, v.capacity
FROM (VALUES
    ('Cairo Convention Center',     'Nasr City',  'Cairo',      30.0731, 31.3417, 2000),
    ('Alexandria Bibliotheca Hall','Corniche',   'Alexandria', 31.2089, 29.9092, 800),
    ('GrEEK Campus',               'Downtown',   'Cairo',      30.0459, 31.2367, 500)
) AS v(name, address, city, lat, lng, capacity)
WHERE NOT EXISTS (SELECT 1 FROM venues WHERE venues.name = v.name);

-- Demo users (password for all three is "demo123", bcrypt-hashed)
INSERT INTO users (id, full_name, email, password_hash, role, status, email_verified, city, onboarding_completed) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Sarah Ahmed',  'sarah@demo.com', '$2b$10$VL0bZR2xmBvQyoWL83WYbOJKyNIQPCmB/z22vdpH1jnYAQX3xfzvG', 'ATTENDEE',  'ACTIVE', TRUE, 'Cairo', TRUE),
    ('22222222-2222-2222-2222-222222222222', 'Ahmed Hassan', 'ahmed@demo.com', '$2b$10$VL0bZR2xmBvQyoWL83WYbOJKyNIQPCmB/z22vdpH1jnYAQX3xfzvG', 'ORGANIZER', 'ACTIVE', TRUE, 'Cairo', TRUE),
    ('33333333-3333-3333-3333-333333333333', 'Layla Admin',  'admin@demo.com', '$2b$10$VL0bZR2xmBvQyoWL83WYbOJKyNIQPCmB/z22vdpH1jnYAQX3xfzvG', 'ADMIN',     'ACTIVE', TRUE, 'Cairo', TRUE)
ON CONFLICT (email) DO NOTHING;

-- Ahmed's organizer profile — pre-approved and verified so the demo organizer can publish immediately
INSERT INTO organizer_profiles (user_id, organization_name, organization_description, approved_by, approved_at, is_verified)
SELECT '22222222-2222-2222-2222-222222222222', 'Ahmed Events Co.',
       'Full-service event organizing collective based in Cairo.',
       '33333333-3333-3333-3333-333333333333', now(), TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM organizer_profiles WHERE user_id = '22222222-2222-2222-2222-222222222222'
);


