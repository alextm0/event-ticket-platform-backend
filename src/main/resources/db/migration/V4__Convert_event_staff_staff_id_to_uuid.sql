BEGIN;

-- Drop foreign keys referencing users.id so we can change column types
ALTER TABLE events
    DROP CONSTRAINT IF EXISTS events_organizer_id_fkey;
ALTER TABLE events
    DROP CONSTRAINT IF EXISTS fk_events_organizer;

ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS orders_user_id_fkey;
ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS fk_orders_user;

ALTER TABLE event_staff
    DROP CONSTRAINT IF EXISTS fk_event_staff_user;
ALTER TABLE event_staff
    DROP CONSTRAINT IF EXISTS event_staff_staff_id_fkey;

-- Drop PK constraint on users so we can alter id type
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_pkey CASCADE;

-- Convert primary key column on users from TEXT to UUID
ALTER TABLE users
    ALTER COLUMN id TYPE UUID
        USING id::uuid;

-- Recreate primary key constraint
ALTER TABLE users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

-- Convert referencing columns to UUID as well
ALTER TABLE events
    ALTER COLUMN organizer_id TYPE UUID
        USING organizer_id::uuid;

ALTER TABLE orders
    ALTER COLUMN user_id TYPE UUID
        USING user_id::uuid;

ALTER TABLE event_staff
    ALTER COLUMN staff_id TYPE UUID
        USING staff_id::uuid;

-- Re-create foreign keys with proper cascade rules
ALTER TABLE events
    ADD CONSTRAINT events_organizer_id_fkey
        FOREIGN KEY (organizer_id)
            REFERENCES users (id);

ALTER TABLE orders
    ADD CONSTRAINT orders_user_id_fkey
        FOREIGN KEY (user_id)
            REFERENCES users (id);

ALTER TABLE event_staff
    ADD CONSTRAINT fk_event_staff_user
        FOREIGN KEY (staff_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

COMMIT;

