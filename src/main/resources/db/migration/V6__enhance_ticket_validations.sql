ALTER TABLE ticket_validations ALTER COLUMN ticket_id DROP NOT NULL;

ALTER TABLE ticket_validations ADD COLUMN qr_code_data TEXT;
ALTER TABLE ticket_validations ADD COLUMN event_id UUID;
ALTER TABLE ticket_validations ADD COLUMN staff_id UUID;

ALTER TABLE ticket_validations ADD CONSTRAINT fk_ticket_validations_events FOREIGN KEY (event_id) REFERENCES events(id);
ALTER TABLE ticket_validations ADD CONSTRAINT fk_ticket_validations_users FOREIGN KEY (staff_id) REFERENCES users(id);

COMMENT ON COLUMN ticket_validations.ticket_id IS 'Nullable - will be NULL for invalid/external QR codes';
