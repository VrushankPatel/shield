# Generated Schema

This file is generated from `db/model/phase5_schema.json`.

## Tables
### `system_log`

- Columns:
  - `tenant_id` `UUID` NULL references `tenant(id)`
  - `user_id` `UUID` NULL references `users(id)`
  - `log_level` `VARCHAR(20)` NOT NULL
  - `logger_name` `VARCHAR(255)` NOT NULL
  - `message` `TEXT` NOT NULL
  - `exception_trace` `TEXT` NULL
  - `endpoint` `VARCHAR(255)` NULL
  - `correlation_id` `VARCHAR(100)` NULL

### `api_request_log`

- Columns:
  - `request_id` `VARCHAR(100)` NULL
  - `tenant_id` `UUID` NULL references `tenant(id)`
  - `user_id` `UUID` NULL references `users(id)`
  - `endpoint` `VARCHAR(255)` NOT NULL
  - `http_method` `VARCHAR(10)` NOT NULL
  - `request_body` `TEXT` NULL
  - `response_status` `INTEGER` NOT NULL
  - `response_time_ms` `BIGINT` NOT NULL
  - `ip_address` `VARCHAR(50)` NULL
  - `user_agent` `VARCHAR(1000)` NULL
