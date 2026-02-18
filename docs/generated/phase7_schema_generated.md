# Generated Schema

This file is generated from `db/model/phase7_schema.json`.

## Tables
### `payment_gateway_txn`

- Tenant-scoped: `true`
- Columns:
  - `transaction_ref` `VARCHAR(120)` NOT NULL unique
  - `bill_id` `UUID` NOT NULL references `maintenance_bill(id)`
  - `provider` `VARCHAR(50)` NOT NULL default `'MANUAL_SIMULATOR'`
  - `gateway_order_id` `VARCHAR(120)` NULL
  - `gateway_payment_id` `VARCHAR(120)` NULL
  - `amount` `NUMERIC(12, 2)` NOT NULL
  - `currency` `VARCHAR(10)` NOT NULL default `'INR'`
  - `mode` `VARCHAR(50)` NOT NULL
  - `status` `VARCHAR(32)` NOT NULL default `'CREATED'`
  - `callback_payload` `TEXT` NULL
  - `failure_reason` `VARCHAR(500)` NULL
  - `initiated_by` `UUID` NULL references `users(id)`
  - `verified_by` `UUID` NULL references `users(id)`
  - `verified_at` `TIMESTAMP` NULL
