ALTER TABLE unit
    ADD COLUMN ownership_status VARCHAR(20);

UPDATE unit
SET ownership_status = 'OWNED'
WHERE ownership_status IS NULL;

ALTER TABLE unit
    ALTER COLUMN ownership_status SET NOT NULL;

ALTER TABLE unit
    ALTER COLUMN ownership_status SET DEFAULT 'OWNED';
