-- Backfill event_id for existing valid validations
UPDATE ticket_validations tv
SET event_id = (
    SELECT e.id 
    FROM tickets t 
    JOIN ticket_types tt ON t.ticket_type_id = tt.id 
    JOIN events e ON tt.event_id = e.id 
    WHERE t.id = tv.ticket_id
);
