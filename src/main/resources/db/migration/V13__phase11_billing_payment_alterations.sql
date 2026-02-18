-- M3 billing/payment expansion adjustments for existing payment table

ALTER TABLE payment ALTER COLUMN bill_id DROP NOT NULL;

ALTER TABLE payment
    ADD COLUMN invoice_id UUID REFERENCES invoice (id),
    ADD COLUMN unit_id UUID REFERENCES unit (id),
    ADD COLUMN payment_status VARCHAR(50),
    ADD COLUMN receipt_url VARCHAR(1000),
    ADD COLUMN payment_gateway VARCHAR(50),
    ADD COLUMN refunded_at TIMESTAMP,
    ADD COLUMN refund_reason VARCHAR(500);

CREATE INDEX idx_payment_invoice ON payment (tenant_id, invoice_id);
CREATE INDEX idx_payment_unit ON payment (tenant_id, unit_id);
CREATE INDEX idx_payment_status ON payment (tenant_id, payment_status);
