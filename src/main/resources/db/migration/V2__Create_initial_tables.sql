-- ----------------------------
-- ENUM Types
-- ----------------------------
CREATE TYPE user_role AS ENUM (
    'ORGANIZER',
    'STAFF',
    'ATTENDEE'
);

CREATE TYPE event_status AS ENUM (
    'DRAFT',
    'PUBLISHED',
    'CANCELLED'
);

CREATE TYPE order_status AS ENUM (
    'PENDING',
    'PAID',
    'CANCELLED'
);

CREATE TYPE ticket_status AS ENUM (
    'PURCHASED',
    'CHECKED_IN'
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'ATTENDEE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------
-- Table structure for events
-- (Depends on: users)
-- ----------------------------
CREATE TABLE events (
    id UUID PRIMARY KEY,
    organizer_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    status event_status NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_events_organizer
        FOREIGN KEY (organizer_id) REFERENCES users(id)
);

-- ----------------------------
-- Table structure for ticket_types
-- (Depends on: events)
-- ----------------------------
CREATE TABLE ticket_types (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    total_quantity INTEGER NOT NULL,
    sold_count INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_ticket_types_event
        FOREIGN KEY (event_id) REFERENCES events(id)
);

-- ----------------------------
-- Table structure for orders
-- (Depends on: users)
-- ----------------------------
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    user_id UUID,
    buyer_name VARCHAR(255),
    buyer_email VARCHAR(255),
    total_amount DECIMAL(10, 2) NOT NULL,
    status order_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_order_buyer CHECK (
        user_id IS NOT NULL
        OR (user_id IS NULL AND buyer_name IS NOT NULL AND buyer_email IS NOT NULL)
    ),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL -- If user is deleted, keep order as guest
);

-- ----------------------------
-- Table structure for tickets
-- (Depends on: orders, ticket_types)
-- ----------------------------
CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    ticket_type_id UUID NOT NULL,
    qr_code VARCHAR(255) UNIQUE NOT NULL,
    status ticket_status NOT NULL DEFAULT 'PURCHASED',
    checked_in_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tickets_order
        FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_tickets_ticket_type
        FOREIGN KEY (ticket_type_id) REFERENCES ticket_types(id)
);

-- ----------------------------
-- Table structure for event_staff
-- (Depends on: events, users)
-- ----------------------------
CREATE TABLE event_staff (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    staff_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_event_staff UNIQUE (event_id, staff_id),
    CONSTRAINT fk_event_staff_event
        FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE, -- If event is deleted, remove staff assignment
    CONSTRAINT fk_event_staff_staff
        FOREIGN KEY (staff_id) REFERENCES users(id) ON DELETE CASCADE -- If staff user is deleted, remove assignment
);
