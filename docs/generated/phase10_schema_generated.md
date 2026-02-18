# Generated Schema

This file is generated from `db/model/phase10_schema.json`.

## Tables
### `app_role`

- Tenant-scoped: `true`
- Columns:
  - `code` `VARCHAR(50)` NOT NULL
  - `name` `VARCHAR(100)` NOT NULL
  - `description` `VARCHAR(500)` NULL
  - `system_role` `BOOLEAN` NOT NULL default `FALSE`

### `permission`

- Tenant-scoped: `true`
- Columns:
  - `code` `VARCHAR(100)` NOT NULL
  - `module_name` `VARCHAR(60)` NULL
  - `description` `VARCHAR(500)` NULL

### `role_permission`

- Tenant-scoped: `true`
- Columns:
  - `role_id` `UUID` NOT NULL references `app_role(id)`
  - `permission_id` `UUID` NOT NULL references `permission(id)`

### `user_additional_role`

- Tenant-scoped: `true`
- Columns:
  - `user_id` `UUID` NOT NULL references `users(id)`
  - `role_id` `UUID` NOT NULL references `app_role(id)`
  - `granted_by` `UUID` NULL references `users(id)`
  - `granted_at` `TIMESTAMP` NOT NULL default `CURRENT_TIMESTAMP`
