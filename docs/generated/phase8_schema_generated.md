# Generated Schema

This file is generated from `db/model/phase8_schema.json`.

## Tables
### `auth_token`

- Tenant-scoped: `true`
- Columns:
  - `user_id` `UUID` NOT NULL references `users(id)`
  - `token_value` `VARCHAR(128)` NOT NULL unique
  - `token_type` `VARCHAR(40)` NOT NULL
  - `expires_at` `TIMESTAMP` NOT NULL
  - `consumed_at` `TIMESTAMP` NULL
  - `metadata` `VARCHAR(500)` NULL

### `kyc_document`

- Tenant-scoped: `true`
- Columns:
  - `user_id` `UUID` NOT NULL references `users(id)`
  - `document_type` `VARCHAR(50)` NOT NULL
  - `document_number` `VARCHAR(100)` NOT NULL
  - `document_url` `VARCHAR(2000)` NULL
  - `verification_status` `VARCHAR(30)` NOT NULL default `'PENDING'`
  - `rejection_reason` `VARCHAR(500)` NULL
  - `verified_at` `TIMESTAMP` NULL
  - `verified_by` `UUID` NULL references `users(id)`

### `move_record`

- Tenant-scoped: `true`
- Columns:
  - `unit_id` `UUID` NOT NULL references `unit(id)`
  - `user_id` `UUID` NOT NULL references `users(id)`
  - `move_type` `VARCHAR(20)` NOT NULL
  - `effective_date` `DATE` NOT NULL
  - `security_deposit` `NUMERIC(12, 2)` NULL
  - `agreement_url` `VARCHAR(2000)` NULL
  - `status` `VARCHAR(30)` NOT NULL default `'PENDING'`
  - `decision_notes` `VARCHAR(500)` NULL
  - `approved_by` `UUID` NULL references `users(id)`
  - `approval_date` `DATE` NULL

### `parking_slot`

- Tenant-scoped: `true`
- Columns:
  - `slot_number` `VARCHAR(50)` NOT NULL unique
  - `parking_type` `VARCHAR(50)` NOT NULL
  - `vehicle_type` `VARCHAR(50)` NOT NULL
  - `unit_id` `UUID` NULL references `unit(id)`
  - `allocated` `BOOLEAN` NOT NULL default `FALSE`
  - `allocated_at` `TIMESTAMP` NULL

### `digital_id_card`

- Tenant-scoped: `true`
- Columns:
  - `user_id` `UUID` NOT NULL references `users(id)`
  - `qr_code_data` `VARCHAR(1000)` NOT NULL unique
  - `qr_code_url` `VARCHAR(2000)` NULL
  - `issue_date` `DATE` NOT NULL
  - `expiry_date` `DATE` NOT NULL
  - `active` `BOOLEAN` NOT NULL default `TRUE`
  - `deactivated_at` `TIMESTAMP` NULL
