# Generated Schema

This file is generated from `db/model/phase12_schema.json`.

## Tables
### `account_head`

- Tenant-scoped: `true`
- Columns:
  - `head_name` `VARCHAR(100)` NOT NULL
  - `head_type` `VARCHAR(50)` NOT NULL
  - `parent_head_id` `UUID` NULL references `account_head(id)`

### `fund_category`

- Tenant-scoped: `true`
- Columns:
  - `category_name` `VARCHAR(100)` NOT NULL
  - `description` `VARCHAR(1000)` NULL
  - `current_balance` `NUMERIC(12, 2)` NOT NULL default `0`

### `vendor`

- Tenant-scoped: `true`
- Columns:
  - `vendor_name` `VARCHAR(255)` NOT NULL
  - `contact_person` `VARCHAR(100)` NULL
  - `phone` `VARCHAR(20)` NULL
  - `email` `VARCHAR(255)` NULL
  - `address` `VARCHAR(1000)` NULL
  - `gstin` `VARCHAR(50)` NULL
  - `pan` `VARCHAR(50)` NULL
  - `vendor_type` `VARCHAR(100)` NULL
  - `active` `BOOLEAN` NOT NULL default `TRUE`

### `expense`

- Tenant-scoped: `true`
- Columns:
  - `expense_number` `VARCHAR(100)` NOT NULL unique
  - `account_head_id` `UUID` NOT NULL references `account_head(id)`
  - `fund_category_id` `UUID` NULL references `fund_category(id)`
  - `vendor_id` `UUID` NULL references `vendor(id)`
  - `expense_date` `DATE` NOT NULL
  - `amount` `NUMERIC(12, 2)` NOT NULL
  - `description` `VARCHAR(1000)` NULL
  - `invoice_number` `VARCHAR(100)` NULL
  - `invoice_url` `VARCHAR(1000)` NULL
  - `payment_status` `VARCHAR(50)` NOT NULL default `'PENDING'`
  - `approved_by` `UUID` NULL references `users(id)`
  - `approval_date` `DATE` NULL

### `vendor_payment`

- Tenant-scoped: `true`
- Columns:
  - `vendor_id` `UUID` NOT NULL references `vendor(id)`
  - `expense_id` `UUID` NULL references `expense(id)`
  - `payment_date` `DATE` NOT NULL
  - `amount` `NUMERIC(12, 2)` NOT NULL
  - `payment_method` `VARCHAR(50)` NULL
  - `transaction_reference` `VARCHAR(255)` NULL
  - `created_by` `UUID` NULL references `users(id)`
  - `status` `VARCHAR(50)` NOT NULL default `'COMPLETED'`

### `budget`

- Tenant-scoped: `true`
- Columns:
  - `financial_year` `VARCHAR(20)` NOT NULL
  - `account_head_id` `UUID` NOT NULL references `account_head(id)`
  - `budgeted_amount` `NUMERIC(12, 2)` NOT NULL
