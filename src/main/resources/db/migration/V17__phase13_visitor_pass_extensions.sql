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
