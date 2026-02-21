# Generated Schema

This file is generated from `db/model/phase19_schema.json`.

## Tables
### `announcement_read_receipt`

- Tenant-scoped: `true`
- Columns:
  - `announcement_id` `UUID` NOT NULL references `announcement(id)`
  - `user_id` `UUID` NOT NULL references `users(id)`
  - `read_at` `TIMESTAMP` NOT NULL default `CURRENT_TIMESTAMP`
