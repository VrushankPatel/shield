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
