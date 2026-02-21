# Generated Schema

This file is generated from `db/model/phase20_schema.json`.

## Tables
### `unit_ownership_history`

- Tenant-scoped: `true`
- Columns:
  - `unit_id` `UUID` NOT NULL references `unit(id)`
  - `previous_ownership_status` `VARCHAR(20)` NOT NULL
  - `new_ownership_status` `VARCHAR(20)` NOT NULL
  - `changed_by` `UUID` NULL references `users(id)`
  - `changed_at` `TIMESTAMP` NOT NULL default `CURRENT_TIMESTAMP`
  - `notes` `VARCHAR(500)` NULL
