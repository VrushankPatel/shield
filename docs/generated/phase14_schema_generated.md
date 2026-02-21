# Generated Schema

This file is generated from `db/model/phase14_schema.json`.

## Tables
### `asset_category`

- Tenant-scoped: `true`
- Columns:
  - `category_name` `VARCHAR(100)` NOT NULL
  - `description` `VARCHAR(1000)` NULL

### `complaint_comment`

- Tenant-scoped: `true`
- Columns:
  - `complaint_id` `UUID` NOT NULL references `complaint(id)`
  - `user_id` `UUID` NOT NULL references `users(id)`
  - `comment` `VARCHAR(2000)` NOT NULL

### `work_order`

- Tenant-scoped: `true`
- Columns:
  - `work_order_number` `VARCHAR(100)` NOT NULL unique
  - `complaint_id` `UUID` NOT NULL references `complaint(id)`
  - `asset_id` `UUID` NULL references `asset(id)`
  - `vendor_id` `UUID` NULL references `vendor(id)`
  - `work_description` `VARCHAR(2000)` NOT NULL
  - `estimated_cost` `NUMERIC(12,2)` NULL
  - `actual_cost` `NUMERIC(12,2)` NULL
  - `scheduled_date` `DATE` NULL
  - `completion_date` `DATE` NULL
  - `status` `VARCHAR(40)` NOT NULL
  - `created_by` `UUID` NULL references `users(id)`

### `preventive_maintenance_schedule`

- Tenant-scoped: `true`
- Columns:
  - `asset_id` `UUID` NOT NULL references `asset(id)`
  - `maintenance_type` `VARCHAR(120)` NOT NULL
  - `frequency` `VARCHAR(40)` NOT NULL
  - `last_maintenance_date` `DATE` NULL
  - `next_maintenance_date` `DATE` NOT NULL
  - `assigned_vendor_id` `UUID` NULL references `vendor(id)`
  - `active` `BOOLEAN` NOT NULL default `TRUE`

### `asset_depreciation`

- Tenant-scoped: `true`
- Columns:
  - `asset_id` `UUID` NOT NULL references `asset(id)`
  - `depreciation_method` `VARCHAR(40)` NOT NULL
  - `depreciation_rate` `NUMERIC(6,2)` NOT NULL
  - `depreciation_year` `INTEGER` NOT NULL
  - `depreciation_amount` `NUMERIC(12,2)` NOT NULL
  - `book_value` `NUMERIC(12,2)` NOT NULL
