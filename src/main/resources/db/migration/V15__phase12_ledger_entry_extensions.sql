-- M4 accounting/treasury ledger extensions

ALTER TABLE ledger_entry
    ADD COLUMN account_head_id UUID REFERENCES account_head(id),
    ADD COLUMN fund_category_id UUID REFERENCES fund_category(id),
    ADD COLUMN transaction_type VARCHAR(50),
    ADD COLUMN reference_type VARCHAR(50),
    ADD COLUMN reference_id UUID,
    ADD COLUMN created_by UUID REFERENCES users(id);

CREATE INDEX idx_ledger_entry_account_head ON ledger_entry (tenant_id, account_head_id);
CREATE INDEX idx_ledger_entry_fund_category ON ledger_entry (tenant_id, fund_category_id);
CREATE INDEX idx_ledger_entry_transaction_type ON ledger_entry (tenant_id, transaction_type);
CREATE INDEX idx_ledger_entry_entry_date ON ledger_entry (tenant_id, entry_date);
