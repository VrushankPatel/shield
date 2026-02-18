-- M6 asset and complaint table enrichment for advanced module-6 APIs

ALTER TABLE asset
    ADD COLUMN asset_name VARCHAR(255),
    ADD COLUMN category_id UUID REFERENCES asset_category(id),
    ADD COLUMN block_name VARCHAR(50),
    ADD COLUMN floor_label VARCHAR(50),
    ADD COLUMN installation_date DATE,
    ADD COLUMN warranty_expiry_date DATE,
    ADD COLUMN amc_applicable BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN amc_vendor_id UUID REFERENCES vendor(id),
    ADD COLUMN amc_start_date DATE,
    ADD COLUMN amc_end_date DATE,
    ADD COLUMN purchase_cost NUMERIC(12,2),
    ADD COLUMN current_value NUMERIC(12,2),
    ADD COLUMN qr_code_data VARCHAR(500);

CREATE INDEX idx_asset_category_id ON asset (tenant_id, category_id);
CREATE INDEX idx_asset_warranty_expiry ON asset (tenant_id, warranty_expiry_date);
CREATE INDEX idx_asset_amc_end_date ON asset (tenant_id, amc_end_date);
CREATE INDEX idx_asset_qr_code_data ON asset (tenant_id, qr_code_data);

ALTER TABLE complaint
    ADD COLUMN complaint_number VARCHAR(100),
    ADD COLUMN raised_by UUID REFERENCES users(id),
    ADD COLUMN complaint_type VARCHAR(100),
    ADD COLUMN location VARCHAR(255),
    ADD COLUMN assigned_at TIMESTAMP,
    ADD COLUMN resolution_notes VARCHAR(2000),
    ADD COLUMN closed_at TIMESTAMP,
    ADD COLUMN sla_hours INTEGER,
    ADD COLUMN sla_breach BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE complaint
SET complaint_number = 'CMP-' || upper(substr(id::text, 1, 8))
WHERE complaint_number IS NULL;

ALTER TABLE complaint
    ALTER COLUMN complaint_number SET NOT NULL;

CREATE UNIQUE INDEX uk_complaint_tenant_number ON complaint (tenant_id, complaint_number);
CREATE INDEX idx_complaint_status ON complaint (tenant_id, status);
CREATE INDEX idx_complaint_priority ON complaint (tenant_id, priority);
CREATE INDEX idx_complaint_asset ON complaint (tenant_id, asset_id);
CREATE INDEX idx_complaint_assigned_to ON complaint (tenant_id, assigned_to);
CREATE INDEX idx_complaint_raised_by ON complaint (tenant_id, raised_by);
CREATE INDEX idx_complaint_sla_breach ON complaint (tenant_id, sla_breach);
