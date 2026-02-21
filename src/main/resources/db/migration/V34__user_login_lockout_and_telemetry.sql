ALTER TABLE users
    ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_failed_login_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_failed_login_ip VARCHAR(64),
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_login_ip VARCHAR(64),
    ADD COLUMN IF NOT EXISTS last_login_user_agent VARCHAR(512);

CREATE INDEX IF NOT EXISTS idx_users_locked_until
    ON users (locked_until)
    WHERE deleted = FALSE;
