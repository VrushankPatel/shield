# Generated Schema

This file is generated from `db/model/phase11_schema.json`.

## Tables
### `billing_cycle`

- Tenant-scoped: `true`
- Columns:
  - `cycle_name` `VARCHAR(100)` NOT NULL
  - `month` `INTEGER` NOT NULL
  - `year` `INTEGER` NOT NULL
  - `due_date` `DATE` NOT NULL
  - `late_fee_applicable_date` `DATE` NULL
  - `status` `VARCHAR(30)` NOT NULL

### `maintenance_charge`

- Tenant-scoped: `true`
- Columns:
  - `unit_id` `UUID` NOT NULL references `unit(id)`
  - `billing_cycle_id` `UUID` NOT NULL references `billing_cycle(id)`
  - `base_amount` `NUMERIC(12, 2)` NULL
  - `calculation_method` `VARCHAR(50)` NULL
  - `area_based_amount` `NUMERIC(12, 2)` NULL
  - `fixed_amount` `NUMERIC(12, 2)` NULL
  - `total_amount` `NUMERIC(12, 2)` NOT NULL

### `special_assessment`

- Tenant-scoped: `true`
- Columns:
  - `assessment_name` `VARCHAR(255)` NOT NULL
  - `description` `VARCHAR(1000)` NULL
  - `total_amount` `NUMERIC(12, 2)` NOT NULL
  - `per_unit_amount` `NUMERIC(12, 2)` NULL
  - `assessment_date` `DATE` NULL
  - `due_date` `DATE` NOT NULL
  - `created_by` `UUID` NULL references `users(id)`
  - `status` `VARCHAR(30)` NOT NULL default `'ACTIVE'`

### `invoice`

- Tenant-scoped: `true`
- Columns:
  - `invoice_number` `VARCHAR(100)` NOT NULL unique
  - `unit_id` `UUID` NOT NULL references `unit(id)`
  - `billing_cycle_id` `UUID` NULL references `billing_cycle(id)`
  - `invoice_date` `DATE` NOT NULL
  - `due_date` `DATE` NOT NULL
  - `subtotal` `NUMERIC(12, 2)` NOT NULL
  - `late_fee` `NUMERIC(12, 2)` NOT NULL default `0`
  - `gst_amount` `NUMERIC(12, 2)` NOT NULL default `0`
  - `other_charges` `NUMERIC(12, 2)` NOT NULL default `0`
  - `total_amount` `NUMERIC(12, 2)` NOT NULL
  - `outstanding_amount` `NUMERIC(12, 2)` NOT NULL
  - `status` `VARCHAR(30)` NOT NULL

### `payment_reminder`

- Tenant-scoped: `true`
- Columns:
  - `invoice_id` `UUID` NOT NULL references `invoice(id)`
  - `reminder_type` `VARCHAR(50)` NOT NULL
  - `sent_at` `TIMESTAMP` NULL
  - `channel` `VARCHAR(50)` NOT NULL
  - `status` `VARCHAR(30)` NOT NULL

### `late_fee_rule`

- Tenant-scoped: `true`
- Columns:
  - `rule_name` `VARCHAR(100)` NOT NULL
  - `days_after_due` `INTEGER` NOT NULL
  - `fee_type` `VARCHAR(30)` NOT NULL
  - `fee_amount` `NUMERIC(12, 2)` NOT NULL
  - `active` `BOOLEAN` NOT NULL default `TRUE`
