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
