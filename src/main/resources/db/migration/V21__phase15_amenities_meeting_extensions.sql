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
