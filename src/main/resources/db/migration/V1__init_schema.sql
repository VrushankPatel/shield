CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE tenant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE unit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    unit_number VARCHAR(50) NOT NULL,
    block_name VARCHAR(50),
    unit_type VARCHAR(50),
    square_feet NUMERIC(12, 2),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_unit_tenant_number UNIQUE (tenant_id, unit_number, block_name)
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    unit_id UUID,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(32),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_user_tenant_email UNIQUE (tenant_id, email)
);

CREATE TABLE maintenance_bill (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    unit_id UUID NOT NULL,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    late_fee NUMERIC(12, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE payment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    bill_id UUID NOT NULL REFERENCES maintenance_bill (id),
    amount NUMERIC(12, 2) NOT NULL,
    mode VARCHAR(50) NOT NULL,
    transaction_ref VARCHAR(100),
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE ledger_entry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    type VARCHAR(20) NOT NULL,
    category VARCHAR(80) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    reference VARCHAR(128),
    description VARCHAR(1000),
    entry_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE visitor_pass (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    unit_id UUID NOT NULL,
    visitor_name VARCHAR(200) NOT NULL,
    vehicle_number VARCHAR(40),
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    qr_code VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE asset (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    asset_code VARCHAR(80) NOT NULL,
    category VARCHAR(80) NOT NULL,
    location VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    purchase_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_asset_tenant_code UNIQUE (tenant_id, asset_code)
);

CREATE TABLE complaint (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    asset_id UUID,
    unit_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    assigned_to UUID,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE amenity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    name VARCHAR(150) NOT NULL,
    capacity INTEGER NOT NULL,
    requires_approval BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE amenity_booking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    amenity_id UUID NOT NULL REFERENCES amenity (id),
    unit_id UUID NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE meeting (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    title VARCHAR(255) NOT NULL,
    agenda VARCHAR(2000),
    scheduled_at TIMESTAMP NOT NULL,
    minutes VARCHAR(8000),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID,
    action VARCHAR(128) NOT NULL,
    entity_type VARCHAR(128) NOT NULL,
    entity_id UUID,
    payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_unit_tenant ON unit (tenant_id);
CREATE INDEX idx_user_tenant ON users (tenant_id);
CREATE INDEX idx_bill_tenant ON maintenance_bill (tenant_id);
CREATE INDEX idx_payment_tenant ON payment (tenant_id);
CREATE INDEX idx_ledger_tenant ON ledger_entry (tenant_id);
CREATE INDEX idx_visitor_tenant ON visitor_pass (tenant_id);
CREATE INDEX idx_asset_tenant ON asset (tenant_id);
CREATE INDEX idx_complaint_tenant ON complaint (tenant_id);
CREATE INDEX idx_amenity_tenant ON amenity (tenant_id);
CREATE INDEX idx_amenity_booking_tenant ON amenity_booking (tenant_id);
CREATE INDEX idx_meeting_tenant ON meeting (tenant_id);
CREATE INDEX idx_audit_tenant ON audit_log (tenant_id);
