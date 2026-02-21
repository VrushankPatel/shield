-- SHIELD consolidated database model
-- Generated from Flyway migrations in src/main/resources/db/migration
-- Generated at 2026-02-21T11:02:35Z

-- ===========================================================================
-- Source: src/main/resources/db/migration/V1__init_schema.sql
-- ===========================================================================
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

-- ===========================================================================
-- Source: src/main/resources/db/migration/V2__announcement_and_notification.sql
-- ===========================================================================
CREATE TABLE announcement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    is_emergency BOOLEAN NOT NULL DEFAULT FALSE,
    published_by UUID,
    published_at TIMESTAMP,
    expires_at TIMESTAMP,
    target_audience VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE notification_preference (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    user_id UUID NOT NULL REFERENCES users (id),
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_notification_pref_tenant_user UNIQUE (tenant_id, user_id)
);

CREATE TABLE notification_email_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    user_id UUID,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message VARCHAR(1000),
    source_type VARCHAR(80),
    source_id UUID,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_announcement_tenant ON announcement (tenant_id);
CREATE INDEX idx_announcement_status ON announcement (tenant_id, status);

CREATE INDEX idx_notification_pref_tenant ON notification_preference (tenant_id);
CREATE INDEX idx_notification_email_log_tenant ON notification_email_log (tenant_id);
CREATE INDEX idx_notification_email_log_source ON notification_email_log (source_type, source_id);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V3__phase2_generated_modules.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase2_schema.json

CREATE TABLE helpdesk_category (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    sla_hours INTEGER,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_helpdesk_category_tenant ON helpdesk_category (tenant_id);

CREATE TABLE helpdesk_ticket (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    ticket_number VARCHAR(100) NOT NULL UNIQUE,
    category_id UUID REFERENCES helpdesk_category(id),
    raised_by UUID REFERENCES users(id),
    unit_id UUID,
    subject VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL,
    assigned_to UUID REFERENCES users(id),
    assigned_at TIMESTAMP,
    resolved_at TIMESTAMP,
    resolution_notes VARCHAR(2000),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_helpdesk_ticket_tenant ON helpdesk_ticket (tenant_id);
CREATE INDEX idx_helpdesk_ticket_status ON helpdesk_ticket (tenant_id, status);
CREATE INDEX idx_helpdesk_ticket_assigned ON helpdesk_ticket (tenant_id, assigned_to);

CREATE TABLE helpdesk_comment (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    ticket_id UUID NOT NULL REFERENCES helpdesk_ticket(id),
    user_id UUID REFERENCES users(id),
    comment VARCHAR(2000) NOT NULL,
    internal_note BOOLEAN NOT NULL DEFAULT FALSE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_helpdesk_comment_tenant ON helpdesk_comment (tenant_id);
CREATE INDEX idx_helpdesk_comment_ticket ON helpdesk_comment (ticket_id);

CREATE TABLE emergency_contact (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    contact_type VARCHAR(100) NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    phone_primary VARCHAR(20) NOT NULL,
    phone_secondary VARCHAR(20),
    address VARCHAR(500),
    display_order INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_emergency_contact_tenant ON emergency_contact (tenant_id);

CREATE TABLE sos_alert (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    alert_number VARCHAR(100) NOT NULL UNIQUE,
    raised_by UUID REFERENCES users(id),
    unit_id UUID,
    alert_type VARCHAR(100) NOT NULL,
    location VARCHAR(255),
    description VARCHAR(2000),
    latitude NUMERIC(10, 8),
    longitude NUMERIC(11, 8),
    status VARCHAR(50) NOT NULL,
    responded_by UUID REFERENCES users(id),
    responded_at TIMESTAMP,
    resolved_at TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_sos_alert_tenant ON sos_alert (tenant_id);
CREATE INDEX idx_sos_alert_status ON sos_alert (tenant_id, status);

CREATE TABLE document_category (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    category_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    parent_category_id UUID REFERENCES document_category(id),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_document_category_tenant ON document_category (tenant_id);

CREATE TABLE document (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    document_name VARCHAR(255) NOT NULL,
    category_id UUID REFERENCES document_category(id),
    document_type VARCHAR(50) NOT NULL,
    file_url VARCHAR(2000) NOT NULL,
    file_size BIGINT,
    description VARCHAR(1000),
    version_label VARCHAR(50),
    public_access BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_by UUID REFERENCES users(id),
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_date DATE,
    tags VARCHAR(500),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_document_tenant ON document (tenant_id);
CREATE INDEX idx_document_category ON document (tenant_id, category_id);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V4__phase2_staff_utility_marketplace_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase3_schema.json

CREATE TABLE staff (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    designation VARCHAR(100) NOT NULL,
    date_of_joining DATE NOT NULL,
    date_of_leaving DATE,
    employment_type VARCHAR(50) NOT NULL,
    basic_salary NUMERIC(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_staff_tenant ON staff (tenant_id);
CREATE INDEX idx_staff_active ON staff (tenant_id, active);

CREATE TABLE staff_attendance (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    staff_id UUID NOT NULL REFERENCES staff(id),
    attendance_date DATE NOT NULL,
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    marked_by UUID REFERENCES users(id),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_staff_attendance_tenant ON staff_attendance (tenant_id);
CREATE INDEX idx_staff_attendance_staff_date ON staff_attendance (staff_id, attendance_date);

CREATE TABLE payroll (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    staff_id UUID NOT NULL REFERENCES staff(id),
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    working_days INTEGER NOT NULL,
    present_days INTEGER NOT NULL,
    gross_salary NUMERIC(10, 2) NOT NULL,
    total_deductions NUMERIC(10, 2) NOT NULL DEFAULT 0,
    net_salary NUMERIC(10, 2) NOT NULL,
    payment_date DATE,
    payment_method VARCHAR(50),
    payment_reference VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    payslip_url VARCHAR(2000),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_payroll_tenant ON payroll (tenant_id);
CREATE INDEX idx_payroll_staff_period ON payroll (staff_id, year, month);

CREATE TABLE water_tank (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    tank_name VARCHAR(100) NOT NULL,
    tank_type VARCHAR(50) NOT NULL,
    capacity NUMERIC(10, 2) NOT NULL,
    location VARCHAR(255),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_water_tank_tenant ON water_tank (tenant_id);

CREATE TABLE water_level_log (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    tank_id UUID NOT NULL REFERENCES water_tank(id),
    reading_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    level_percentage NUMERIC(5, 2) NOT NULL,
    volume NUMERIC(10, 2),
    recorded_by UUID REFERENCES users(id),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_water_level_log_tenant ON water_level_log (tenant_id);
CREATE INDEX idx_water_level_log_tank_time ON water_level_log (tank_id, reading_time);

CREATE TABLE electricity_meter (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    meter_number VARCHAR(100) NOT NULL UNIQUE,
    meter_type VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    unit_id UUID,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_electricity_meter_tenant ON electricity_meter (tenant_id);

CREATE TABLE electricity_reading (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    meter_id UUID NOT NULL REFERENCES electricity_meter(id),
    reading_date DATE NOT NULL,
    reading_value NUMERIC(10, 2) NOT NULL,
    units_consumed NUMERIC(10, 2),
    cost NUMERIC(10, 2),
    recorded_by UUID REFERENCES users(id),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_electricity_reading_tenant ON electricity_reading (tenant_id);
CREATE INDEX idx_electricity_reading_meter_date ON electricity_reading (meter_id, reading_date);

CREATE TABLE marketplace_category (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    category_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_marketplace_category_tenant ON marketplace_category (tenant_id);

CREATE TABLE marketplace_listing (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    listing_number VARCHAR(100) NOT NULL UNIQUE,
    category_id UUID REFERENCES marketplace_category(id),
    listing_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2),
    negotiable BOOLEAN NOT NULL DEFAULT FALSE,
    images VARCHAR(2000),
    posted_by UUID REFERENCES users(id),
    unit_id UUID,
    status VARCHAR(50) NOT NULL,
    views_count INTEGER NOT NULL DEFAULT 0,
    expires_at TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_marketplace_listing_tenant ON marketplace_listing (tenant_id);
CREATE INDEX idx_marketplace_listing_status ON marketplace_listing (tenant_id, status);
CREATE INDEX idx_marketplace_listing_posted_by ON marketplace_listing (tenant_id, posted_by);

CREATE TABLE marketplace_inquiry (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    listing_id UUID NOT NULL REFERENCES marketplace_listing(id),
    inquired_by UUID REFERENCES users(id),
    message VARCHAR(2000),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_marketplace_inquiry_tenant ON marketplace_inquiry (tenant_id);
CREATE INDEX idx_marketplace_inquiry_listing ON marketplace_inquiry (listing_id);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V5__phase3_analytics_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase4_schema.json

CREATE TABLE report_template (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    template_name VARCHAR(255) NOT NULL,
    report_type VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    query_template TEXT,
    parameters_json TEXT,
    created_by UUID REFERENCES users(id),
    system_template BOOLEAN NOT NULL DEFAULT FALSE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_report_template_tenant ON report_template (tenant_id);
CREATE INDEX idx_report_template_type ON report_template (tenant_id, report_type);

CREATE TABLE scheduled_report (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    template_id UUID NOT NULL REFERENCES report_template(id),
    report_name VARCHAR(255) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    recipients VARCHAR(2000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_generated_at TIMESTAMP,
    next_generation_at TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_scheduled_report_tenant ON scheduled_report (tenant_id);
CREATE INDEX idx_scheduled_report_active ON scheduled_report (tenant_id, active);

CREATE TABLE analytics_dashboard (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    dashboard_name VARCHAR(255) NOT NULL,
    dashboard_type VARCHAR(100) NOT NULL,
    widgets_json TEXT,
    created_by UUID REFERENCES users(id),
    default_dashboard BOOLEAN NOT NULL DEFAULT FALSE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_analytics_dashboard_tenant ON analytics_dashboard (tenant_id);
CREATE INDEX idx_analytics_dashboard_type ON analytics_dashboard (tenant_id, dashboard_type);
CREATE INDEX idx_analytics_dashboard_default ON analytics_dashboard (tenant_id, default_dashboard);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V6__phase4_log_observability_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase5_schema.json

CREATE TABLE system_log (
    tenant_id UUID REFERENCES tenant(id),
    user_id UUID REFERENCES users(id),
    log_level VARCHAR(20) NOT NULL,
    logger_name VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    exception_trace TEXT,
    endpoint VARCHAR(255),
    correlation_id VARCHAR(100),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_system_log_tenant ON system_log (tenant_id);
CREATE INDEX idx_system_log_level ON system_log (log_level);
CREATE INDEX idx_system_log_created_at ON system_log (created_at);

CREATE TABLE api_request_log (
    request_id VARCHAR(100),
    tenant_id UUID REFERENCES tenant(id),
    user_id UUID REFERENCES users(id),
    endpoint VARCHAR(255) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    request_body TEXT,
    response_status INTEGER NOT NULL,
    response_time_ms BIGINT NOT NULL,
    ip_address VARCHAR(50),
    user_agent VARCHAR(1000),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_api_request_log_tenant ON api_request_log (tenant_id);
CREATE INDEX idx_api_request_log_user ON api_request_log (user_id);
CREATE INDEX idx_api_request_log_status ON api_request_log (response_status);
CREATE INDEX idx_api_request_log_endpoint ON api_request_log (endpoint);
CREATE INDEX idx_api_request_log_created_at ON api_request_log (created_at);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V7__phase5_config_files_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase6_schema.json

CREATE TABLE tenant_config (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    category VARCHAR(50),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_tenant_config_tenant ON tenant_config (tenant_id);
CREATE INDEX idx_tenant_config_key ON tenant_config (tenant_id, config_key);
CREATE INDEX idx_tenant_config_category ON tenant_config (tenant_id, category);

CREATE TABLE system_setting (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    setting_key VARCHAR(120) NOT NULL,
    setting_value TEXT,
    setting_group VARCHAR(80),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_system_setting_tenant ON system_setting (tenant_id);
CREATE INDEX idx_system_setting_key ON system_setting (tenant_id, setting_key);
CREATE INDEX idx_system_setting_group ON system_setting (tenant_id, setting_group);

CREATE TABLE stored_file (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    file_id VARCHAR(120) NOT NULL UNIQUE,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(150),
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(2000) NOT NULL,
    uploaded_by UUID REFERENCES users(id),
    checksum VARCHAR(128),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_stored_file_tenant ON stored_file (tenant_id);
CREATE INDEX idx_stored_file_uploaded_by ON stored_file (tenant_id, uploaded_by);
CREATE INDEX idx_stored_file_status ON stored_file (tenant_id, status);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V8__phase6_payment_gateway_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase7_schema.json

CREATE TABLE payment_gateway_txn (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    transaction_ref VARCHAR(120) NOT NULL UNIQUE,
    bill_id UUID NOT NULL REFERENCES maintenance_bill(id),
    provider VARCHAR(50) NOT NULL DEFAULT 'MANUAL_SIMULATOR',
    gateway_order_id VARCHAR(120),
    gateway_payment_id VARCHAR(120),
    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    mode VARCHAR(50) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    callback_payload TEXT,
    failure_reason VARCHAR(500),
    initiated_by UUID REFERENCES users(id),
    verified_by UUID REFERENCES users(id),
    verified_at TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_payment_gateway_txn_tenant ON payment_gateway_txn (tenant_id);
CREATE INDEX idx_payment_gateway_txn_bill ON payment_gateway_txn (tenant_id, bill_id);
CREATE INDEX idx_payment_gateway_txn_status ON payment_gateway_txn (tenant_id, status);
CREATE INDEX idx_payment_gateway_txn_provider ON payment_gateway_txn (tenant_id, provider);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V9__phase8_identity_extensions_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase8_schema.json

CREATE TABLE auth_token (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    user_id UUID NOT NULL REFERENCES users(id),
    token_value VARCHAR(128) NOT NULL UNIQUE,
    token_type VARCHAR(40) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    metadata VARCHAR(500),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_auth_token_tenant ON auth_token (tenant_id);
CREATE INDEX idx_auth_token_user_type ON auth_token (tenant_id, user_id, token_type);
CREATE INDEX idx_auth_token_expires ON auth_token (tenant_id, expires_at);

CREATE TABLE kyc_document (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    user_id UUID NOT NULL REFERENCES users(id),
    document_type VARCHAR(50) NOT NULL,
    document_number VARCHAR(100) NOT NULL,
    document_url VARCHAR(2000),
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    verified_at TIMESTAMP,
    verified_by UUID REFERENCES users(id),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_kyc_document_tenant ON kyc_document (tenant_id);
CREATE INDEX idx_kyc_document_user ON kyc_document (tenant_id, user_id);
CREATE INDEX idx_kyc_document_status ON kyc_document (tenant_id, verification_status);

CREATE TABLE move_record (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    unit_id UUID NOT NULL REFERENCES unit(id),
    user_id UUID NOT NULL REFERENCES users(id),
    move_type VARCHAR(20) NOT NULL,
    effective_date DATE NOT NULL,
    security_deposit NUMERIC(12, 2),
    agreement_url VARCHAR(2000),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    decision_notes VARCHAR(500),
    approved_by UUID REFERENCES users(id),
    approval_date DATE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_move_record_tenant ON move_record (tenant_id);
CREATE INDEX idx_move_record_unit ON move_record (tenant_id, unit_id);
CREATE INDEX idx_move_record_status ON move_record (tenant_id, status);

CREATE TABLE parking_slot (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    slot_number VARCHAR(50) NOT NULL UNIQUE,
    parking_type VARCHAR(50) NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL,
    unit_id UUID REFERENCES unit(id),
    allocated BOOLEAN NOT NULL DEFAULT FALSE,
    allocated_at TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_parking_slot_tenant ON parking_slot (tenant_id);
CREATE INDEX idx_parking_slot_allocated ON parking_slot (tenant_id, allocated);
CREATE INDEX idx_parking_slot_unit ON parking_slot (tenant_id, unit_id);

CREATE TABLE digital_id_card (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    user_id UUID NOT NULL REFERENCES users(id),
    qr_code_data VARCHAR(1000) NOT NULL UNIQUE,
    qr_code_url VARCHAR(2000),
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_digital_id_card_tenant ON digital_id_card (tenant_id);
CREATE INDEX idx_digital_id_card_user ON digital_id_card (tenant_id, user_id);
CREATE INDEX idx_digital_id_card_active ON digital_id_card (tenant_id, active);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V10__phase10_iam_rbac_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase10_schema.json

CREATE TABLE app_role (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    system_role BOOLEAN NOT NULL DEFAULT FALSE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_app_role_tenant ON app_role (tenant_id);
CREATE INDEX idx_app_role_code ON app_role (tenant_id, code);

CREATE TABLE permission (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    code VARCHAR(100) NOT NULL,
    module_name VARCHAR(60),
    description VARCHAR(500),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_permission_tenant ON permission (tenant_id);
CREATE INDEX idx_permission_code ON permission (tenant_id, code);

CREATE TABLE role_permission (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    role_id UUID NOT NULL REFERENCES app_role(id),
    permission_id UUID NOT NULL REFERENCES permission(id),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_role_permission_tenant ON role_permission (tenant_id);
CREATE INDEX idx_role_permission_role ON role_permission (tenant_id, role_id);
CREATE INDEX idx_role_permission_permission ON role_permission (tenant_id, permission_id);

CREATE TABLE user_additional_role (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    user_id UUID NOT NULL REFERENCES users(id),
    role_id UUID NOT NULL REFERENCES app_role(id),
    granted_by UUID REFERENCES users(id),
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_user_additional_role_tenant ON user_additional_role (tenant_id);
CREATE INDEX idx_user_additional_role_user ON user_additional_role (tenant_id, user_id);
CREATE INDEX idx_user_additional_role_role ON user_additional_role (tenant_id, role_id);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V11__phase9_communication_completion.sql
-- ===========================================================================
-- M2: Communication Module Completion
-- Tables: announcement_attachment, poll, poll_option, poll_vote, newsletter
-- Alteration: notification_email_log + read_at

CREATE TABLE announcement_attachment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    announcement_id UUID NOT NULL REFERENCES announcement (id),
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE poll (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    multiple_choice BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE poll_option (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    poll_id UUID NOT NULL REFERENCES poll (id),
    option_text VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE poll_vote (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    poll_id UUID NOT NULL REFERENCES poll (id),
    option_id UUID NOT NULL REFERENCES poll_option (id),
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_poll_vote_user_poll UNIQUE (poll_id, user_id)
);

CREATE TABLE newsletter (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    title VARCHAR(255) NOT NULL,
    content TEXT,
    summary VARCHAR(1000),
    file_url VARCHAR(1000),
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    published_at TIMESTAMP,
    published_by UUID,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Notification extension: read tracking
ALTER TABLE notification_email_log ADD COLUMN read_at TIMESTAMP;

-- Indexes
CREATE INDEX idx_ann_attach_tenant ON announcement_attachment (tenant_id);
CREATE INDEX idx_ann_attach_announcement ON announcement_attachment (announcement_id);

CREATE INDEX idx_poll_tenant ON poll (tenant_id);
CREATE INDEX idx_poll_status ON poll (tenant_id, status);

CREATE INDEX idx_poll_option_poll ON poll_option (poll_id);

CREATE INDEX idx_poll_vote_poll ON poll_vote (poll_id);
CREATE INDEX idx_poll_vote_user ON poll_vote (poll_id, user_id);

CREATE INDEX idx_newsletter_tenant ON newsletter (tenant_id);
CREATE INDEX idx_newsletter_year ON newsletter (tenant_id, year);

CREATE INDEX idx_notification_email_log_user_read ON notification_email_log (user_id, read_at);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V12__phase11_billing_expansion_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase11_schema.json

CREATE TABLE billing_cycle (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    cycle_name VARCHAR(100) NOT NULL,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    due_date DATE NOT NULL,
    late_fee_applicable_date DATE,
    status VARCHAR(30) NOT NULL,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_billing_cycle_tenant ON billing_cycle (tenant_id);
CREATE INDEX idx_billing_cycle_year_month ON billing_cycle (tenant_id, year, month);
CREATE INDEX idx_billing_cycle_status ON billing_cycle (tenant_id, status);

CREATE TABLE maintenance_charge (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    unit_id UUID NOT NULL REFERENCES unit(id),
    billing_cycle_id UUID NOT NULL REFERENCES billing_cycle(id),
    base_amount NUMERIC(12, 2),
    calculation_method VARCHAR(50),
    area_based_amount NUMERIC(12, 2),
    fixed_amount NUMERIC(12, 2),
    total_amount NUMERIC(12, 2) NOT NULL,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_maintenance_charge_tenant ON maintenance_charge (tenant_id);
CREATE INDEX idx_maintenance_charge_cycle ON maintenance_charge (tenant_id, billing_cycle_id);
CREATE INDEX idx_maintenance_charge_unit ON maintenance_charge (tenant_id, unit_id);

CREATE TABLE special_assessment (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    assessment_name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    total_amount NUMERIC(12, 2) NOT NULL,
    per_unit_amount NUMERIC(12, 2),
    assessment_date DATE,
    due_date DATE NOT NULL,
    created_by UUID REFERENCES users(id),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_special_assessment_tenant ON special_assessment (tenant_id);
CREATE INDEX idx_special_assessment_due ON special_assessment (tenant_id, due_date);
CREATE INDEX idx_special_assessment_status ON special_assessment (tenant_id, status);

CREATE TABLE invoice (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    invoice_number VARCHAR(100) NOT NULL UNIQUE,
    unit_id UUID NOT NULL REFERENCES unit(id),
    billing_cycle_id UUID REFERENCES billing_cycle(id),
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,
    late_fee NUMERIC(12, 2) NOT NULL DEFAULT 0,
    gst_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    other_charges NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12, 2) NOT NULL,
    outstanding_amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_invoice_tenant ON invoice (tenant_id);
CREATE INDEX idx_invoice_unit ON invoice (tenant_id, unit_id);
CREATE INDEX idx_invoice_cycle ON invoice (tenant_id, billing_cycle_id);
CREATE INDEX idx_invoice_status ON invoice (tenant_id, status);

CREATE TABLE payment_reminder (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    invoice_id UUID NOT NULL REFERENCES invoice(id),
    reminder_type VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP,
    channel VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_payment_reminder_tenant ON payment_reminder (tenant_id);
CREATE INDEX idx_payment_reminder_invoice ON payment_reminder (tenant_id, invoice_id);
CREATE INDEX idx_payment_reminder_status ON payment_reminder (tenant_id, status);

CREATE TABLE late_fee_rule (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    rule_name VARCHAR(100) NOT NULL,
    days_after_due INTEGER NOT NULL,
    fee_type VARCHAR(30) NOT NULL,
    fee_amount NUMERIC(12, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_late_fee_rule_tenant ON late_fee_rule (tenant_id);
CREATE INDEX idx_late_fee_rule_active ON late_fee_rule (tenant_id, active);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V13__phase11_billing_payment_alterations.sql
-- ===========================================================================
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

-- ===========================================================================
-- Source: src/main/resources/db/migration/V14__phase12_accounting_treasury_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase12_schema.json

CREATE TABLE account_head (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    head_name VARCHAR(100) NOT NULL,
    head_type VARCHAR(50) NOT NULL,
    parent_head_id UUID REFERENCES account_head(id),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_account_head_tenant ON account_head (tenant_id);
CREATE INDEX idx_account_head_type ON account_head (tenant_id, head_type);
CREATE INDEX idx_account_head_parent ON account_head (tenant_id, parent_head_id);

CREATE TABLE fund_category (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    category_name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    current_balance NUMERIC(12, 2) NOT NULL DEFAULT 0,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_fund_category_tenant ON fund_category (tenant_id);
CREATE INDEX idx_fund_category_name ON fund_category (tenant_id, category_name);

CREATE TABLE vendor (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    vendor_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    address VARCHAR(1000),
    gstin VARCHAR(50),
    pan VARCHAR(50),
    vendor_type VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_vendor_tenant ON vendor (tenant_id);
CREATE INDEX idx_vendor_type ON vendor (tenant_id, vendor_type);
CREATE INDEX idx_vendor_active ON vendor (tenant_id, active);

CREATE TABLE expense (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    expense_number VARCHAR(100) NOT NULL UNIQUE,
    account_head_id UUID NOT NULL REFERENCES account_head(id),
    fund_category_id UUID REFERENCES fund_category(id),
    vendor_id UUID REFERENCES vendor(id),
    expense_date DATE NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    description VARCHAR(1000),
    invoice_number VARCHAR(100),
    invoice_url VARCHAR(1000),
    payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    approved_by UUID REFERENCES users(id),
    approval_date DATE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_expense_tenant ON expense (tenant_id);
CREATE INDEX idx_expense_vendor ON expense (tenant_id, vendor_id);
CREATE INDEX idx_expense_account_head ON expense (tenant_id, account_head_id);
CREATE INDEX idx_expense_status ON expense (tenant_id, payment_status);
CREATE INDEX idx_expense_date ON expense (tenant_id, expense_date);

CREATE TABLE vendor_payment (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    vendor_id UUID NOT NULL REFERENCES vendor(id),
    expense_id UUID REFERENCES expense(id),
    payment_date DATE NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    payment_method VARCHAR(50),
    transaction_reference VARCHAR(255),
    created_by UUID REFERENCES users(id),
    status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_vendor_payment_tenant ON vendor_payment (tenant_id);
CREATE INDEX idx_vendor_payment_vendor ON vendor_payment (tenant_id, vendor_id);
CREATE INDEX idx_vendor_payment_expense ON vendor_payment (tenant_id, expense_id);
CREATE INDEX idx_vendor_payment_status ON vendor_payment (tenant_id, status);

CREATE TABLE budget (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    financial_year VARCHAR(20) NOT NULL,
    account_head_id UUID NOT NULL REFERENCES account_head(id),
    budgeted_amount NUMERIC(12, 2) NOT NULL,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_budget_tenant ON budget (tenant_id);
CREATE INDEX idx_budget_financial_year ON budget (tenant_id, financial_year);
CREATE INDEX idx_budget_account_head ON budget (tenant_id, account_head_id);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V15__phase12_ledger_entry_extensions.sql
-- ===========================================================================
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

-- ===========================================================================
-- Source: src/main/resources/db/migration/V16__phase13_visitor_expansion_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase13_schema.json

CREATE TABLE visitor (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    visitor_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    vehicle_number VARCHAR(50),
    visitor_type VARCHAR(50),
    id_proof_type VARCHAR(50),
    id_proof_number VARCHAR(100),
    photo_url VARCHAR(1000),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_visitor_registry_tenant ON visitor (tenant_id);
CREATE INDEX idx_visitor_registry_phone ON visitor (tenant_id, phone);
CREATE INDEX idx_visitor_registry_type ON visitor (tenant_id, visitor_type);

CREATE TABLE visitor_entry_exit_log (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    visitor_pass_id UUID NOT NULL REFERENCES visitor_pass(id),
    entry_time TIMESTAMP,
    exit_time TIMESTAMP,
    entry_gate VARCHAR(50),
    exit_gate VARCHAR(50),
    security_guard_entry UUID REFERENCES users(id),
    security_guard_exit UUID REFERENCES users(id),
    face_capture_url VARCHAR(1000),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_visitor_log_tenant ON visitor_entry_exit_log (tenant_id);
CREATE INDEX idx_visitor_log_pass ON visitor_entry_exit_log (tenant_id, visitor_pass_id);
CREATE INDEX idx_visitor_log_entry_time ON visitor_entry_exit_log (tenant_id, entry_time);
CREATE INDEX idx_visitor_log_exit_time ON visitor_entry_exit_log (tenant_id, exit_time);

CREATE TABLE domestic_help_registry (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    help_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    help_type VARCHAR(50),
    permanent_pass BOOLEAN NOT NULL DEFAULT FALSE,
    police_verification_done BOOLEAN NOT NULL DEFAULT FALSE,
    verification_date DATE,
    photo_url VARCHAR(1000),
    registered_by UUID REFERENCES users(id),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_domestic_help_tenant ON domestic_help_registry (tenant_id);
CREATE INDEX idx_domestic_help_type ON domestic_help_registry (tenant_id, help_type);
CREATE INDEX idx_domestic_help_phone ON domestic_help_registry (tenant_id, phone);

CREATE TABLE domestic_help_unit_mapping (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    domestic_help_id UUID NOT NULL REFERENCES domestic_help_registry(id),
    unit_id UUID NOT NULL REFERENCES unit(id),
    start_date DATE,
    end_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_domestic_help_unit_tenant ON domestic_help_unit_mapping (tenant_id);
CREATE INDEX idx_domestic_help_unit_help ON domestic_help_unit_mapping (tenant_id, domestic_help_id);
CREATE INDEX idx_domestic_help_unit_unit ON domestic_help_unit_mapping (tenant_id, unit_id);
CREATE INDEX idx_domestic_help_unit_active ON domestic_help_unit_mapping (tenant_id, active);

CREATE TABLE blacklist (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    person_name VARCHAR(255),
    phone VARCHAR(20),
    reason VARCHAR(1000),
    blacklisted_by UUID REFERENCES users(id),
    blacklist_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_blacklist_tenant ON blacklist (tenant_id);
CREATE INDEX idx_blacklist_phone ON blacklist (tenant_id, phone);
CREATE INDEX idx_blacklist_active ON blacklist (tenant_id, active);

CREATE TABLE delivery_log (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    unit_id UUID NOT NULL REFERENCES unit(id),
    delivery_partner VARCHAR(100) NOT NULL,
    tracking_number VARCHAR(100),
    delivery_time TIMESTAMP NOT NULL,
    received_by UUID REFERENCES users(id),
    security_guard_id UUID REFERENCES users(id),
    photo_url VARCHAR(1000),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_delivery_log_tenant ON delivery_log (tenant_id);
CREATE INDEX idx_delivery_log_unit ON delivery_log (tenant_id, unit_id);
CREATE INDEX idx_delivery_log_partner ON delivery_log (tenant_id, delivery_partner);
CREATE INDEX idx_delivery_log_time ON delivery_log (tenant_id, delivery_time);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V17__phase13_visitor_pass_extensions.sql
-- ===========================================================================
-- M5 visitor management enrichments for existing visitor_pass table

ALTER TABLE visitor_pass
    ADD COLUMN pass_number VARCHAR(100),
    ADD COLUMN visitor_id UUID REFERENCES visitor(id),
    ADD COLUMN approved_by UUID REFERENCES users(id),
    ADD COLUMN visit_date DATE,
    ADD COLUMN purpose VARCHAR(255),
    ADD COLUMN number_of_persons INTEGER NOT NULL DEFAULT 1;

CREATE UNIQUE INDEX idx_visitor_pass_pass_number ON visitor_pass (pass_number);
CREATE INDEX idx_visitor_pass_visitor_id ON visitor_pass (tenant_id, visitor_id);
CREATE INDEX idx_visitor_pass_visit_date ON visitor_pass (tenant_id, visit_date);
CREATE INDEX idx_visitor_pass_status ON visitor_pass (tenant_id, status);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V18__phase14_asset_complaint_expansion_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase14_schema.json

CREATE TABLE asset_category (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    category_name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_asset_category_tenant ON asset_category (tenant_id);
CREATE INDEX idx_asset_category_name ON asset_category (tenant_id, category_name);

CREATE TABLE complaint_comment (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    complaint_id UUID NOT NULL REFERENCES complaint(id),
    user_id UUID NOT NULL REFERENCES users(id),
    comment VARCHAR(2000) NOT NULL,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_complaint_comment_tenant ON complaint_comment (tenant_id);
CREATE INDEX idx_complaint_comment_complaint ON complaint_comment (tenant_id, complaint_id);
CREATE INDEX idx_complaint_comment_user ON complaint_comment (tenant_id, user_id);

CREATE TABLE work_order (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    work_order_number VARCHAR(100) NOT NULL UNIQUE,
    complaint_id UUID NOT NULL REFERENCES complaint(id),
    asset_id UUID REFERENCES asset(id),
    vendor_id UUID REFERENCES vendor(id),
    work_description VARCHAR(2000) NOT NULL,
    estimated_cost NUMERIC(12,2),
    actual_cost NUMERIC(12,2),
    scheduled_date DATE,
    completion_date DATE,
    status VARCHAR(40) NOT NULL,
    created_by UUID REFERENCES users(id),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_work_order_tenant ON work_order (tenant_id);
CREATE INDEX idx_work_order_complaint ON work_order (tenant_id, complaint_id);
CREATE INDEX idx_work_order_vendor ON work_order (tenant_id, vendor_id);
CREATE INDEX idx_work_order_status ON work_order (tenant_id, status);

CREATE TABLE preventive_maintenance_schedule (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    asset_id UUID NOT NULL REFERENCES asset(id),
    maintenance_type VARCHAR(120) NOT NULL,
    frequency VARCHAR(40) NOT NULL,
    last_maintenance_date DATE,
    next_maintenance_date DATE NOT NULL,
    assigned_vendor_id UUID REFERENCES vendor(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_preventive_maintenance_tenant ON preventive_maintenance_schedule (tenant_id);
CREATE INDEX idx_preventive_maintenance_asset ON preventive_maintenance_schedule (tenant_id, asset_id);
CREATE INDEX idx_preventive_maintenance_due ON preventive_maintenance_schedule (tenant_id, next_maintenance_date);
CREATE INDEX idx_preventive_maintenance_active ON preventive_maintenance_schedule (tenant_id, active);

CREATE TABLE asset_depreciation (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    asset_id UUID NOT NULL REFERENCES asset(id),
    depreciation_method VARCHAR(40) NOT NULL,
    depreciation_rate NUMERIC(6,2) NOT NULL,
    depreciation_year INTEGER NOT NULL,
    depreciation_amount NUMERIC(12,2) NOT NULL,
    book_value NUMERIC(12,2) NOT NULL,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_asset_depreciation_tenant ON asset_depreciation (tenant_id);
CREATE INDEX idx_asset_depreciation_asset ON asset_depreciation (tenant_id, asset_id);
CREATE INDEX idx_asset_depreciation_year ON asset_depreciation (tenant_id, depreciation_year);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V19__phase14_asset_complaint_extensions.sql
-- ===========================================================================
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

-- ===========================================================================
-- Source: src/main/resources/db/migration/V20__phase15_amenities_meeting_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase15_schema.json

CREATE TABLE amenity_time_slot (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    amenity_id UUID NOT NULL REFERENCES amenity(id),
    slot_name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_amenity_time_slot_tenant ON amenity_time_slot (tenant_id);
CREATE INDEX idx_amenity_time_slot_amenity ON amenity_time_slot (tenant_id, amenity_id);
CREATE INDEX idx_amenity_time_slot_active ON amenity_time_slot (tenant_id, active);

CREATE TABLE amenity_pricing (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    amenity_id UUID NOT NULL REFERENCES amenity(id),
    time_slot_id UUID NOT NULL REFERENCES amenity_time_slot(id),
    day_type VARCHAR(50) NOT NULL,
    base_price NUMERIC(12,2) NOT NULL,
    peak_hour BOOLEAN NOT NULL DEFAULT FALSE,
    peak_hour_multiplier NUMERIC(5,2),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_amenity_pricing_tenant ON amenity_pricing (tenant_id);
CREATE INDEX idx_amenity_pricing_amenity ON amenity_pricing (tenant_id, amenity_id);
CREATE INDEX idx_amenity_pricing_slot ON amenity_pricing (tenant_id, time_slot_id);

CREATE TABLE amenity_booking_rule (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    amenity_id UUID NOT NULL REFERENCES amenity(id),
    rule_type VARCHAR(100) NOT NULL,
    rule_value VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_amenity_booking_rule_tenant ON amenity_booking_rule (tenant_id);
CREATE INDEX idx_amenity_booking_rule_amenity ON amenity_booking_rule (tenant_id, amenity_id);

CREATE TABLE amenity_cancellation_policy (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    amenity_id UUID NOT NULL REFERENCES amenity(id),
    days_before_booking INTEGER NOT NULL,
    refund_percentage NUMERIC(6,2) NOT NULL,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_amenity_cancel_policy_tenant ON amenity_cancellation_policy (tenant_id);
CREATE INDEX idx_amenity_cancel_policy_amenity ON amenity_cancellation_policy (tenant_id, amenity_id);

CREATE TABLE meeting_agenda (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    meeting_id UUID NOT NULL REFERENCES meeting(id),
    agenda_item VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    display_order INTEGER NOT NULL,
    presenter UUID REFERENCES users(id),
    estimated_duration INTEGER,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_meeting_agenda_tenant ON meeting_agenda (tenant_id);
CREATE INDEX idx_meeting_agenda_meeting ON meeting_agenda (tenant_id, meeting_id);

CREATE TABLE meeting_attendee (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    meeting_id UUID NOT NULL REFERENCES meeting(id),
    user_id UUID NOT NULL REFERENCES users(id),
    invitation_sent_at TIMESTAMP,
    rsvp_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    attendance_status VARCHAR(50),
    joined_at TIMESTAMP,
    left_at TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_meeting_attendee_tenant ON meeting_attendee (tenant_id);
CREATE INDEX idx_meeting_attendee_meeting ON meeting_attendee (tenant_id, meeting_id);
CREATE INDEX idx_meeting_attendee_user ON meeting_attendee (tenant_id, user_id);

CREATE TABLE meeting_minutes_record (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    meeting_id UUID NOT NULL REFERENCES meeting(id),
    minutes_content TEXT NOT NULL,
    summary VARCHAR(4000),
    ai_generated_summary VARCHAR(4000),
    prepared_by UUID REFERENCES users(id),
    approved_by UUID REFERENCES users(id),
    approval_date DATE,
    document_url VARCHAR(1000),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_meeting_minutes_tenant ON meeting_minutes_record (tenant_id);
CREATE INDEX idx_meeting_minutes_meeting ON meeting_minutes_record (tenant_id, meeting_id);

CREATE TABLE meeting_resolution (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    meeting_id UUID NOT NULL REFERENCES meeting(id),
    resolution_number VARCHAR(100) NOT NULL,
    resolution_text TEXT NOT NULL,
    proposed_by UUID REFERENCES users(id),
    seconded_by UUID REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    votes_for INTEGER NOT NULL DEFAULT 0,
    votes_against INTEGER NOT NULL DEFAULT 0,
    votes_abstain INTEGER NOT NULL DEFAULT 0,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_meeting_resolution_tenant ON meeting_resolution (tenant_id);
CREATE INDEX idx_meeting_resolution_meeting ON meeting_resolution (tenant_id, meeting_id);
CREATE INDEX idx_meeting_resolution_status ON meeting_resolution (tenant_id, status);

CREATE TABLE meeting_vote (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    resolution_id UUID NOT NULL REFERENCES meeting_resolution(id),
    user_id UUID NOT NULL REFERENCES users(id),
    vote VARCHAR(30) NOT NULL,
    voted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_meeting_vote_tenant ON meeting_vote (tenant_id);
CREATE INDEX idx_meeting_vote_resolution ON meeting_vote (tenant_id, resolution_id);
CREATE INDEX idx_meeting_vote_user ON meeting_vote (tenant_id, user_id);

CREATE TABLE meeting_action_item (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    meeting_id UUID NOT NULL REFERENCES meeting(id),
    action_description VARCHAR(2000) NOT NULL,
    assigned_to UUID REFERENCES users(id),
    due_date DATE,
    priority VARCHAR(20),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    completion_date DATE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_meeting_action_item_tenant ON meeting_action_item (tenant_id);
CREATE INDEX idx_meeting_action_item_meeting ON meeting_action_item (tenant_id, meeting_id);
CREATE INDEX idx_meeting_action_item_assigned ON meeting_action_item (tenant_id, assigned_to);
CREATE INDEX idx_meeting_action_item_status ON meeting_action_item (tenant_id, status);

CREATE TABLE meeting_reminder (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    meeting_id UUID NOT NULL REFERENCES meeting(id),
    reminder_type VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_meeting_reminder_tenant ON meeting_reminder (tenant_id);
CREATE INDEX idx_meeting_reminder_meeting ON meeting_reminder (tenant_id, meeting_id);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V21__phase15_amenities_meeting_extensions.sql
-- ===========================================================================
-- M7 amenities and meeting enrichments for baseline tables

ALTER TABLE amenity
    ADD COLUMN amenity_type VARCHAR(100),
    ADD COLUMN description VARCHAR(1000),
    ADD COLUMN location VARCHAR(255),
    ADD COLUMN booking_allowed BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN advance_booking_days INTEGER NOT NULL DEFAULT 30,
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_amenity_type ON amenity (tenant_id, amenity_type);
CREATE INDEX idx_amenity_active ON amenity (tenant_id, active);

ALTER TABLE amenity_booking
    ADD COLUMN booking_number VARCHAR(100),
    ADD COLUMN time_slot_id UUID REFERENCES amenity_time_slot(id),
    ADD COLUMN booked_by UUID REFERENCES users(id),
    ADD COLUMN booking_date DATE,
    ADD COLUMN number_of_persons INTEGER,
    ADD COLUMN purpose VARCHAR(255),
    ADD COLUMN booking_amount NUMERIC(12,2),
    ADD COLUMN security_deposit NUMERIC(12,2),
    ADD COLUMN payment_status VARCHAR(50),
    ADD COLUMN approved_by UUID REFERENCES users(id),
    ADD COLUMN approval_date TIMESTAMP,
    ADD COLUMN cancellation_date TIMESTAMP,
    ADD COLUMN cancellation_reason VARCHAR(1000);

UPDATE amenity_booking
SET booking_number = 'ABK-' || upper(substr(id::text, 1, 8))
WHERE booking_number IS NULL;

ALTER TABLE amenity_booking
    ALTER COLUMN booking_number SET NOT NULL;

CREATE UNIQUE INDEX uk_amenity_booking_number ON amenity_booking (booking_number);
CREATE INDEX idx_amenity_booking_amenity ON amenity_booking (tenant_id, amenity_id);
CREATE INDEX idx_amenity_booking_status ON amenity_booking (tenant_id, status);
CREATE INDEX idx_amenity_booking_booked_by ON amenity_booking (tenant_id, booked_by);
CREATE INDEX idx_amenity_booking_date ON amenity_booking (tenant_id, booking_date);

ALTER TABLE meeting
    ADD COLUMN meeting_number VARCHAR(100),
    ADD COLUMN meeting_type VARCHAR(100),
    ADD COLUMN location VARCHAR(255),
    ADD COLUMN meeting_mode VARCHAR(50),
    ADD COLUMN meeting_link VARCHAR(1000),
    ADD COLUMN created_by UUID REFERENCES users(id);

UPDATE meeting
SET meeting_number = 'MTG-' || upper(substr(id::text, 1, 8))
WHERE meeting_number IS NULL;

ALTER TABLE meeting
    ALTER COLUMN meeting_number SET NOT NULL;

CREATE UNIQUE INDEX uk_meeting_number ON meeting (meeting_number);
CREATE INDEX idx_meeting_type ON meeting (tenant_id, meeting_type);
CREATE INDEX idx_meeting_status ON meeting (tenant_id, status);
CREATE INDEX idx_meeting_scheduled_at ON meeting (tenant_id, scheduled_at);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V22__phase16_staff_payroll_completion_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase16_schema.json

CREATE TABLE staff_leave (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    staff_id UUID NOT NULL REFERENCES staff(id),
    leave_type VARCHAR(50) NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    number_of_days INTEGER NOT NULL,
    reason VARCHAR(2000),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    approved_by UUID REFERENCES users(id),
    approval_date DATE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_staff_leave_tenant ON staff_leave (tenant_id);
CREATE INDEX idx_staff_leave_staff ON staff_leave (tenant_id, staff_id);
CREATE INDEX idx_staff_leave_status ON staff_leave (tenant_id, status);
CREATE INDEX idx_staff_leave_dates ON staff_leave (tenant_id, from_date, to_date);

CREATE TABLE payroll_component (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    component_name VARCHAR(100) NOT NULL,
    component_type VARCHAR(50) NOT NULL,
    taxable BOOLEAN NOT NULL DEFAULT TRUE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_payroll_component_tenant ON payroll_component (tenant_id);
CREATE INDEX idx_payroll_component_name ON payroll_component (tenant_id, component_name);
CREATE INDEX idx_payroll_component_type ON payroll_component (tenant_id, component_type);

CREATE TABLE staff_salary_structure (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    staff_id UUID NOT NULL REFERENCES staff(id),
    payroll_component_id UUID NOT NULL REFERENCES payroll_component(id),
    amount NUMERIC(12,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_staff_salary_structure_tenant ON staff_salary_structure (tenant_id);
CREATE INDEX idx_staff_salary_structure_staff ON staff_salary_structure (tenant_id, staff_id);
CREATE INDEX idx_staff_salary_structure_component ON staff_salary_structure (tenant_id, payroll_component_id);
CREATE INDEX idx_staff_salary_structure_active ON staff_salary_structure (tenant_id, active);

CREATE TABLE payroll_detail (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    payroll_id UUID NOT NULL REFERENCES payroll(id),
    payroll_component_id UUID NOT NULL REFERENCES payroll_component(id),
    amount NUMERIC(12,2) NOT NULL,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_payroll_detail_tenant ON payroll_detail (tenant_id);
CREATE INDEX idx_payroll_detail_payroll ON payroll_detail (tenant_id, payroll_id);
CREATE INDEX idx_payroll_detail_component ON payroll_detail (tenant_id, payroll_component_id);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V23__phase17_m9_expansion_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase17_schema.json

CREATE TABLE helpdesk_ticket_attachment (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    ticket_id UUID NOT NULL REFERENCES helpdesk_ticket(id),
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(2000) NOT NULL,
    uploaded_by UUID REFERENCES users(id),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_helpdesk_ticket_attachment_tenant ON helpdesk_ticket_attachment (tenant_id);
CREATE INDEX idx_helpdesk_ticket_attachment_ticket ON helpdesk_ticket_attachment (tenant_id, ticket_id);

CREATE TABLE fire_drill_record (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    drill_date DATE NOT NULL,
    drill_time TIME,
    conducted_by UUID REFERENCES users(id),
    evacuation_time INTEGER,
    participants_count INTEGER,
    observations VARCHAR(2000),
    report_url VARCHAR(2000),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_fire_drill_record_tenant ON fire_drill_record (tenant_id);
CREATE INDEX idx_fire_drill_record_date ON fire_drill_record (tenant_id, drill_date);

CREATE TABLE safety_equipment (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    equipment_type VARCHAR(100) NOT NULL,
    equipment_tag VARCHAR(100),
    location VARCHAR(255),
    installation_date DATE,
    last_inspection_date DATE,
    next_inspection_date DATE,
    inspection_frequency_days INTEGER,
    functional BOOLEAN NOT NULL DEFAULT TRUE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_safety_equipment_tenant ON safety_equipment (tenant_id);
CREATE INDEX idx_safety_equipment_type ON safety_equipment (tenant_id, equipment_type);
CREATE INDEX idx_safety_equipment_next_inspection ON safety_equipment (tenant_id, next_inspection_date);

CREATE TABLE safety_inspection (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    equipment_id UUID NOT NULL REFERENCES safety_equipment(id),
    inspection_date DATE NOT NULL,
    inspected_by UUID REFERENCES users(id),
    inspection_result VARCHAR(50) NOT NULL,
    remarks VARCHAR(2000),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_safety_inspection_tenant ON safety_inspection (tenant_id);
CREATE INDEX idx_safety_inspection_equipment ON safety_inspection (tenant_id, equipment_id);
CREATE INDEX idx_safety_inspection_date ON safety_inspection (tenant_id, inspection_date);

CREATE TABLE document_access_log (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    document_id UUID NOT NULL REFERENCES document(id),
    accessed_by UUID REFERENCES users(id),
    access_type VARCHAR(50) NOT NULL,
    accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_document_access_log_tenant ON document_access_log (tenant_id);
CREATE INDEX idx_document_access_log_document ON document_access_log (tenant_id, document_id);
CREATE INDEX idx_document_access_log_user ON document_access_log (tenant_id, accessed_by);
CREATE INDEX idx_document_access_log_time ON document_access_log (tenant_id, accessed_at);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V24__phase17_helpdesk_ticket_extensions.sql
-- ===========================================================================
ALTER TABLE helpdesk_ticket
    ADD COLUMN IF NOT EXISTS satisfaction_rating INTEGER,
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_helpdesk_ticket_satisfaction_rating
    ON helpdesk_ticket (tenant_id, satisfaction_rating);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V25__platform_root_account.sql
-- ===========================================================================
CREATE TABLE platform_root_account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    login_id VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    email VARCHAR(255),
    mobile VARCHAR(20),
    email_verified BOOLEAN NOT NULL DEFAULT TRUE,
    mobile_verified BOOLEAN NOT NULL DEFAULT TRUE,
    password_change_required BOOLEAN NOT NULL DEFAULT TRUE,
    token_version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_platform_root_account_login ON platform_root_account (login_id);

INSERT INTO platform_root_account (login_id, email_verified, mobile_verified, password_change_required, token_version, active)
VALUES ('root', TRUE, TRUE, TRUE, 0, TRUE)
ON CONFLICT (login_id) DO NOTHING;

-- ===========================================================================
-- Source: src/main/resources/db/migration/V26__platform_root_login_lockout.sql
-- ===========================================================================
ALTER TABLE platform_root_account
    ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_platform_root_account_locked_until
    ON platform_root_account (locked_until);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V27__phase18_utility_marketplace_extensions_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase18_schema.json

CREATE TABLE diesel_generator (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    generator_name VARCHAR(100) NOT NULL,
    capacity_kva NUMERIC(10, 2) NOT NULL,
    location VARCHAR(255),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_diesel_generator_tenant ON diesel_generator (tenant_id);
CREATE INDEX idx_diesel_generator_name ON diesel_generator (tenant_id, generator_name);

CREATE TABLE generator_log (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    generator_id UUID NOT NULL REFERENCES diesel_generator(id),
    log_date DATE NOT NULL,
    start_time TIMESTAMP,
    stop_time TIMESTAMP,
    runtime_hours NUMERIC(5, 2),
    diesel_consumed NUMERIC(10, 2),
    diesel_cost NUMERIC(10, 2),
    meter_reading_before NUMERIC(10, 2),
    meter_reading_after NUMERIC(10, 2),
    units_generated NUMERIC(10, 2),
    operator_id UUID REFERENCES users(id),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_generator_log_tenant ON generator_log (tenant_id);
CREATE INDEX idx_generator_log_generator_date ON generator_log (tenant_id, generator_id, log_date);
CREATE INDEX idx_generator_log_date ON generator_log (tenant_id, log_date);

CREATE TABLE carpool_listing (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    posted_by UUID REFERENCES users(id),
    route_from VARCHAR(255) NOT NULL,
    route_to VARCHAR(255) NOT NULL,
    departure_time TIME NOT NULL,
    available_seats INTEGER NOT NULL,
    days_of_week VARCHAR(100) NOT NULL,
    vehicle_type VARCHAR(50),
    contact_preference VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_carpool_listing_tenant ON carpool_listing (tenant_id);
CREATE INDEX idx_carpool_listing_posted_by ON carpool_listing (tenant_id, posted_by);
CREATE INDEX idx_carpool_listing_route ON carpool_listing (tenant_id, route_from, route_to);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V28__phase19_announcement_read_receipts_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase19_schema.json

CREATE TABLE announcement_read_receipt (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    announcement_id UUID NOT NULL REFERENCES announcement(id),
    user_id UUID NOT NULL REFERENCES users(id),
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_ann_read_receipt_tenant ON announcement_read_receipt (tenant_id);
CREATE INDEX idx_ann_read_receipt_announcement ON announcement_read_receipt (tenant_id, announcement_id);
CREATE INDEX idx_ann_read_receipt_user ON announcement_read_receipt (tenant_id, user_id);

-- ===========================================================================
-- Source: src/main/resources/db/migration/V29__phase19_announcement_read_receipt_constraints.sql
-- ===========================================================================
CREATE UNIQUE INDEX uk_ann_read_receipt_announcement_user
    ON announcement_read_receipt (tenant_id, announcement_id, user_id)
    WHERE deleted = FALSE;

-- ===========================================================================
-- Source: src/main/resources/db/migration/V30__phase20_unit_ownership_status.sql
-- ===========================================================================
ALTER TABLE unit
    ADD COLUMN ownership_status VARCHAR(20);

UPDATE unit
SET ownership_status = 'OWNED'
WHERE ownership_status IS NULL;

ALTER TABLE unit
    ALTER COLUMN ownership_status SET NOT NULL;

ALTER TABLE unit
    ALTER COLUMN ownership_status SET DEFAULT 'OWNED';

-- ===========================================================================
-- Source: src/main/resources/db/migration/V31__phase20_unit_ownership_history_generated.sql
-- ===========================================================================
-- AUTO-GENERATED FILE. DO NOT EDIT MANUALLY.
-- Generated by scripts/generate_db_artifacts.py from db/model/phase20_schema.json

CREATE TABLE unit_ownership_history (
    tenant_id UUID NOT NULL REFERENCES tenant (id),
    unit_id UUID NOT NULL REFERENCES unit(id),
    previous_ownership_status VARCHAR(20) NOT NULL,
    new_ownership_status VARCHAR(20) NOT NULL,
    changed_by UUID REFERENCES users(id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(500),
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_unit_ownership_history_tenant ON unit_ownership_history (tenant_id);
CREATE INDEX idx_unit_ownership_history_unit ON unit_ownership_history (tenant_id, unit_id);
CREATE INDEX idx_unit_ownership_history_changed_at ON unit_ownership_history (tenant_id, changed_at);

