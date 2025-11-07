-- Core enum types
CREATE TYPE user_role AS ENUM ('ORGANIZER', 'STAFF', 'ATTENDEE');
CREATE TYPE event_status AS ENUM ('DRAFT', 'PUBLISHED', 'CANCELLED');
CREATE TYPE order_status AS ENUM ('PENDING', 'PAID', 'CANCELLED');
CREATE TYPE ticket_status AS ENUM ('PURCHASED', 'CHECKED_IN');
CREATE TYPE qr_code_status AS ENUM ('ACTIVE', 'EXPIRED');
CREATE TYPE ticket_validation_status AS ENUM ('VALID', 'INVALID', 'EXPIRED');
CREATE TYPE ticket_validation_method AS ENUM ('QR_SCAN', 'MANUAL');

-- Users
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'ATTENDEE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Events owned by users
CREATE TABLE events (
    id UUID PRIMARY KEY,
    organizer_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    status event_status NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Ticket catalog for events
CREATE TABLE ticket_types (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    total_quantity INTEGER NOT NULL,
    sold_count INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_ticket_inventory CHECK (
        total_quantity >= 0
        AND sold_count >= 0
        AND sold_count <= total_quantity
    )
);

-- Orders placed by authenticated users
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    buyer_name VARCHAR(255),
    buyer_email VARCHAR(255),
    total_amount DECIMAL(10, 2) NOT NULL,
    status order_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Concrete tickets tied to orders and ticket types
CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id),
    ticket_type_id UUID NOT NULL REFERENCES ticket_types(id),
    qr_code VARCHAR(255) NOT NULL UNIQUE,
    status ticket_status NOT NULL DEFAULT 'PURCHASED',
    checked_in_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Event staff assignments
CREATE TABLE event_staff (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    staff_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_event_staff UNIQUE (event_id, staff_id)
);

-- QR code lifecycle
CREATE TABLE qr_codes (
    id UUID PRIMARY KEY,
    generated_date_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status qr_code_status NOT NULL
);

-- Ticket validation audit
CREATE TABLE ticket_validations (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    status ticket_validation_status NOT NULL,
    validation_date_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    validation_method ticket_validation_method NOT NULL
);
