CREATE TABLE login_rate_limit_bucket (
    bucket_key VARCHAR(255) PRIMARY KEY,
    request_count INTEGER NOT NULL,
    window_start TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_login_rate_limit_bucket_window_start
    ON login_rate_limit_bucket (window_start);
