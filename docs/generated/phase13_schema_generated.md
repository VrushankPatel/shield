# Generated Schema

This file is generated from `db/model/phase13_schema.json`.

## Tables
### `visitor`

- Tenant-scoped: `true`
- Columns:
  - `visitor_name` `VARCHAR(255)` NOT NULL
  - `phone` `VARCHAR(20)` NOT NULL
  - `vehicle_number` `VARCHAR(50)` NULL
  - `visitor_type` `VARCHAR(50)` NULL
  - `id_proof_type` `VARCHAR(50)` NULL
  - `id_proof_number` `VARCHAR(100)` NULL
  - `photo_url` `VARCHAR(1000)` NULL

### `visitor_entry_exit_log`

- Tenant-scoped: `true`
- Columns:
  - `visitor_pass_id` `UUID` NOT NULL references `visitor_pass(id)`
  - `entry_time` `TIMESTAMP` NULL
  - `exit_time` `TIMESTAMP` NULL
  - `entry_gate` `VARCHAR(50)` NULL
  - `exit_gate` `VARCHAR(50)` NULL
  - `security_guard_entry` `UUID` NULL references `users(id)`
  - `security_guard_exit` `UUID` NULL references `users(id)`
  - `face_capture_url` `VARCHAR(1000)` NULL

### `domestic_help_registry`

- Tenant-scoped: `true`
- Columns:
  - `help_name` `VARCHAR(255)` NOT NULL
  - `phone` `VARCHAR(20)` NULL
  - `help_type` `VARCHAR(50)` NULL
  - `permanent_pass` `BOOLEAN` NOT NULL default `FALSE`
  - `police_verification_done` `BOOLEAN` NOT NULL default `FALSE`
  - `verification_date` `DATE` NULL
  - `photo_url` `VARCHAR(1000)` NULL
  - `registered_by` `UUID` NULL references `users(id)`

### `domestic_help_unit_mapping`

- Tenant-scoped: `true`
- Columns:
  - `domestic_help_id` `UUID` NOT NULL references `domestic_help_registry(id)`
  - `unit_id` `UUID` NOT NULL references `unit(id)`
  - `start_date` `DATE` NULL
  - `end_date` `DATE` NULL
  - `active` `BOOLEAN` NOT NULL default `TRUE`

### `blacklist`

- Tenant-scoped: `true`
- Columns:
  - `person_name` `VARCHAR(255)` NULL
  - `phone` `VARCHAR(20)` NULL
  - `reason` `VARCHAR(1000)` NULL
  - `blacklisted_by` `UUID` NULL references `users(id)`
  - `blacklist_date` `DATE` NULL
  - `active` `BOOLEAN` NOT NULL default `TRUE`

### `delivery_log`

- Tenant-scoped: `true`
- Columns:
  - `unit_id` `UUID` NOT NULL references `unit(id)`
  - `delivery_partner` `VARCHAR(100)` NOT NULL
  - `tracking_number` `VARCHAR(100)` NULL
  - `delivery_time` `TIMESTAMP` NOT NULL
  - `received_by` `UUID` NULL references `users(id)`
  - `security_guard_id` `UUID` NULL references `users(id)`
  - `photo_url` `VARCHAR(1000)` NULL
