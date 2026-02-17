# Phase-2 Generated Schema

This file is generated from `db/model/phase2_schema.json`.

## Tables
### `helpdesk_category`

- Tenant-scoped: `true`
- Columns:
  - `name` `VARCHAR(100)` NOT NULL
  - `description` `VARCHAR(500)` NULL
  - `sla_hours` `INTEGER` NULL

### `helpdesk_ticket`

- Tenant-scoped: `true`
- Columns:
  - `ticket_number` `VARCHAR(100)` NOT NULL unique
  - `category_id` `UUID` NULL references `helpdesk_category(id)`
  - `raised_by` `UUID` NULL references `users(id)`
  - `unit_id` `UUID` NULL
  - `subject` `VARCHAR(255)` NOT NULL
  - `description` `TEXT` NULL
  - `priority` `VARCHAR(20)` NOT NULL
  - `status` `VARCHAR(50)` NOT NULL
  - `assigned_to` `UUID` NULL references `users(id)`
  - `assigned_at` `TIMESTAMP` NULL
  - `resolved_at` `TIMESTAMP` NULL
  - `resolution_notes` `VARCHAR(2000)` NULL

### `helpdesk_comment`

- Tenant-scoped: `true`
- Columns:
  - `ticket_id` `UUID` NOT NULL references `helpdesk_ticket(id)`
  - `user_id` `UUID` NULL references `users(id)`
  - `comment` `VARCHAR(2000)` NOT NULL
  - `internal_note` `BOOLEAN` NOT NULL default `FALSE`

### `emergency_contact`

- Tenant-scoped: `true`
- Columns:
  - `contact_type` `VARCHAR(100)` NOT NULL
  - `contact_name` `VARCHAR(255)` NOT NULL
  - `phone_primary` `VARCHAR(20)` NOT NULL
  - `phone_secondary` `VARCHAR(20)` NULL
  - `address` `VARCHAR(500)` NULL
  - `display_order` `INTEGER` NULL
  - `active` `BOOLEAN` NOT NULL default `TRUE`

### `sos_alert`

- Tenant-scoped: `true`
- Columns:
  - `alert_number` `VARCHAR(100)` NOT NULL unique
  - `raised_by` `UUID` NULL references `users(id)`
  - `unit_id` `UUID` NULL
  - `alert_type` `VARCHAR(100)` NOT NULL
  - `location` `VARCHAR(255)` NULL
  - `description` `VARCHAR(2000)` NULL
  - `latitude` `NUMERIC(10, 8)` NULL
  - `longitude` `NUMERIC(11, 8)` NULL
  - `status` `VARCHAR(50)` NOT NULL
  - `responded_by` `UUID` NULL references `users(id)`
  - `responded_at` `TIMESTAMP` NULL
  - `resolved_at` `TIMESTAMP` NULL

### `document_category`

- Tenant-scoped: `true`
- Columns:
  - `category_name` `VARCHAR(100)` NOT NULL
  - `description` `VARCHAR(500)` NULL
  - `parent_category_id` `UUID` NULL references `document_category(id)`

### `document`

- Tenant-scoped: `true`
- Columns:
  - `document_name` `VARCHAR(255)` NOT NULL
  - `category_id` `UUID` NULL references `document_category(id)`
  - `document_type` `VARCHAR(50)` NOT NULL
  - `file_url` `VARCHAR(2000)` NOT NULL
  - `file_size` `BIGINT` NULL
  - `description` `VARCHAR(1000)` NULL
  - `version_label` `VARCHAR(50)` NULL
  - `public_access` `BOOLEAN` NOT NULL default `FALSE`
  - `uploaded_by` `UUID` NULL references `users(id)`
  - `upload_date` `TIMESTAMP` NOT NULL default `CURRENT_TIMESTAMP`
  - `expiry_date` `DATE` NULL
  - `tags` `VARCHAR(500)` NULL
