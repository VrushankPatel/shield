# Generated Schema

This file is generated from `db/model/phase6_schema.json`.

## Tables
### `tenant_config`

- Tenant-scoped: `true`
- Columns:
  - `config_key` `VARCHAR(100)` NOT NULL
  - `config_value` `TEXT` NULL
  - `category` `VARCHAR(50)` NULL

### `system_setting`

- Tenant-scoped: `true`
- Columns:
  - `setting_key` `VARCHAR(120)` NOT NULL
  - `setting_value` `TEXT` NULL
  - `setting_group` `VARCHAR(80)` NULL

### `stored_file`

- Tenant-scoped: `true`
- Columns:
  - `file_id` `VARCHAR(120)` NOT NULL unique
  - `file_name` `VARCHAR(255)` NOT NULL
  - `content_type` `VARCHAR(150)` NULL
  - `file_size` `BIGINT` NOT NULL
  - `storage_path` `VARCHAR(2000)` NOT NULL
  - `uploaded_by` `UUID` NULL references `users(id)`
  - `checksum` `VARCHAR(128)` NULL
  - `status` `VARCHAR(32)` NOT NULL default `'ACTIVE'`
  - `expires_at` `TIMESTAMP` NULL
