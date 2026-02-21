# Generated Schema

This file is generated from `db/model/phase17_schema.json`.

## Tables
### `helpdesk_ticket_attachment`

- Tenant-scoped: `true`
- Columns:
  - `ticket_id` `UUID` NOT NULL references `helpdesk_ticket(id)`
  - `file_name` `VARCHAR(255)` NOT NULL
  - `file_url` `VARCHAR(2000)` NOT NULL
  - `uploaded_by` `UUID` NULL references `users(id)`
  - `uploaded_at` `TIMESTAMP` NOT NULL default `CURRENT_TIMESTAMP`

### `fire_drill_record`

- Tenant-scoped: `true`
- Columns:
  - `drill_date` `DATE` NOT NULL
  - `drill_time` `TIME` NULL
  - `conducted_by` `UUID` NULL references `users(id)`
  - `evacuation_time` `INTEGER` NULL
  - `participants_count` `INTEGER` NULL
  - `observations` `VARCHAR(2000)` NULL
  - `report_url` `VARCHAR(2000)` NULL

### `safety_equipment`

- Tenant-scoped: `true`
- Columns:
  - `equipment_type` `VARCHAR(100)` NOT NULL
  - `equipment_tag` `VARCHAR(100)` NULL
  - `location` `VARCHAR(255)` NULL
  - `installation_date` `DATE` NULL
  - `last_inspection_date` `DATE` NULL
  - `next_inspection_date` `DATE` NULL
  - `inspection_frequency_days` `INTEGER` NULL
  - `functional` `BOOLEAN` NOT NULL default `TRUE`

### `safety_inspection`

- Tenant-scoped: `true`
- Columns:
  - `equipment_id` `UUID` NOT NULL references `safety_equipment(id)`
  - `inspection_date` `DATE` NOT NULL
  - `inspected_by` `UUID` NULL references `users(id)`
  - `inspection_result` `VARCHAR(50)` NOT NULL
  - `remarks` `VARCHAR(2000)` NULL

### `document_access_log`

- Tenant-scoped: `true`
- Columns:
  - `document_id` `UUID` NOT NULL references `document(id)`
  - `accessed_by` `UUID` NULL references `users(id)`
  - `access_type` `VARCHAR(50)` NOT NULL
  - `accessed_at` `TIMESTAMP` NOT NULL default `CURRENT_TIMESTAMP`
