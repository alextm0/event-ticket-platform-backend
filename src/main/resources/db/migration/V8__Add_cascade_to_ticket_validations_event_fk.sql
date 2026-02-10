-- Fix: Allow event deletion when ticket_validations exist.
-- Drop and re-add the foreign key with ON DELETE CASCADE so validations
-- are automatically removed when their event is deleted.

ALTER TABLE ticket_validations DROP CONSTRAINT IF EXISTS fk_ticket_validations_events;

ALTER TABLE ticket_validations ADD CONSTRAINT fk_ticket_validations_events
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE;
