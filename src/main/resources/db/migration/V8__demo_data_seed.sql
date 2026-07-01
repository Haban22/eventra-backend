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

-- Sample published events (organized by Ahmed), one per category, dated in the future
INSERT INTO events (id, organizer_id, title, description, event_datetime, location_address, location_city,
                     location_latitude, location_longitude, venue_id, category_id, capacity_maximum,
                     status, cover_image_url, is_online, online_url)
SELECT gen_random_uuid(), '22222222-2222-2222-2222-222222222222', v.title, v.description, v.event_datetime,
       v.location_address, v.location_city, v.lat, v.lng, ven.id, cat.id, v.capacity, 'PUBLISHED', v.cover_image_url, FALSE, NULL
FROM (VALUES
    ('AI Workshop Cairo',        'Machine learning and AI bootcamp for beginners.',   now() + interval '14 days', 'Nasr City', 'Cairo',      30.0731::double precision, 31.3417::double precision, 'Cairo Convention Center',      'Technology', 200, 'https://picsum.photos/seed/ai-workshop/800/450'),
    ('Startup Networking Night', 'Meet startup founders and entrepreneurs.',          now() + interval '20 days', 'Downtown',  'Cairo',      30.0459::double precision, 31.2367::double precision, 'GrEEK Campus',                 'Business',   150, 'https://picsum.photos/seed/networking/800/450'),
    ('Cairo Jazz Festival',      'Live jazz performances from regional artists.',     now() + interval '25 days', 'Corniche',  'Alexandria', 31.2089::double precision, 29.9092::double precision, 'Alexandria Bibliotheca Hall',  'Music',      600, 'https://picsum.photos/seed/jazz/800/450'),
    ('Nile Run 5K',              'Community fitness run along the Nile.',            now() + interval '10 days', 'Corniche',  'Cairo',      30.0444::double precision, 31.2357::double precision, 'Cairo Convention Center',      'Sports',     500, 'https://picsum.photos/seed/run/800/450'),
    ('Modern Art Exhibition',    'Contemporary Egyptian art showcase.',              now() + interval '18 days', 'Downtown',  'Cairo',      30.0459::double precision, 31.2367::double precision, 'GrEEK Campus',                 'Arts',       300, 'https://picsum.photos/seed/art/800/450'),
    ('Cairo Gaming Expo',        'Esports tournament and gaming showcase.',          now() + interval '30 days', 'Nasr City', 'Cairo',      30.0731::double precision, 31.3417::double precision, 'Cairo Convention Center',      'Gaming',     800, 'https://picsum.photos/seed/gaming/800/450')
) AS v(title, description, event_datetime, location_address, location_city, lat, lng, venue_name, category_name, capacity, cover_image_url)
JOIN venues ven ON ven.name = v.venue_name
JOIN categories cat ON cat.name = v.category_name
WHERE NOT EXISTS (SELECT 1 FROM events WHERE events.title = v.title);

-- Ticket types for each seeded event: GENERAL (full capacity) and VIP (10% of capacity, min 10)
INSERT INTO tickets (id, event_id, ticket_type, price_amount, price_currency, total_available)
SELECT gen_random_uuid(), e.id, 'GENERAL', 150.00, 'EGP', e.capacity_maximum
FROM events e
WHERE e.organizer_id = '22222222-2222-2222-2222-222222222222'
  AND NOT EXISTS (SELECT 1 FROM tickets t WHERE t.event_id = e.id AND t.ticket_type = 'GENERAL');

INSERT INTO tickets (id, event_id, ticket_type, price_amount, price_currency, total_available)
SELECT gen_random_uuid(), e.id, 'VIP', 400.00, 'EGP', GREATEST(e.capacity_maximum / 10, 10)
FROM events e
WHERE e.organizer_id = '22222222-2222-2222-2222-222222222222'
  AND NOT EXISTS (SELECT 1 FROM tickets t WHERE t.event_id = e.id AND t.ticket_type = 'VIP');
