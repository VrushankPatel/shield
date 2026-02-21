CREATE TABLE platform_root_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    root_account_id UUID NOT NULL REFERENCES platform_root_account(id),
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    token_version BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_platform_root_session_account
    ON platform_root_session (root_account_id, consumed_at, deleted);
