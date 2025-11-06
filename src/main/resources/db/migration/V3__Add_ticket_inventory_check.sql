DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        WHERE c.conname = 'chk_ticket_inventory'
          AND c.conrelid = 'ticket_types'::regclass
    ) THEN
        ALTER TABLE ticket_types
            ADD CONSTRAINT chk_ticket_inventory
                CHECK (
                    total_quantity >= 0
                    AND sold_count >= 0
                    AND sold_count <= total_quantity
                );
    END IF;
END$$;
