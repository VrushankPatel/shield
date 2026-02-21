ALTER TABLE helpdesk_ticket
    ADD COLUMN IF NOT EXISTS satisfaction_rating INTEGER,
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_helpdesk_ticket_satisfaction_rating
    ON helpdesk_ticket (tenant_id, satisfaction_rating);
