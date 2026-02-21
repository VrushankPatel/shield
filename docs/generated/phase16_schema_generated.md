# Generated Schema

This file is generated from `db/model/phase16_schema.json`.

## Tables
### `staff_leave`

- Tenant-scoped: `true`
- Columns:
  - `staff_id` `UUID` NOT NULL references `staff(id)`
  - `leave_type` `VARCHAR(50)` NOT NULL
  - `from_date` `DATE` NOT NULL
  - `to_date` `DATE` NOT NULL
  - `number_of_days` `INTEGER` NOT NULL
  - `reason` `VARCHAR(2000)` NULL
  - `status` `VARCHAR(50)` NOT NULL default `'PENDING'`
  - `approved_by` `UUID` NULL references `users(id)`
  - `approval_date` `DATE` NULL

### `payroll_component`

- Tenant-scoped: `true`
- Columns:
  - `component_name` `VARCHAR(100)` NOT NULL
  - `component_type` `VARCHAR(50)` NOT NULL
  - `taxable` `BOOLEAN` NOT NULL default `TRUE`

### `staff_salary_structure`

- Tenant-scoped: `true`
- Columns:
  - `staff_id` `UUID` NOT NULL references `staff(id)`
  - `payroll_component_id` `UUID` NOT NULL references `payroll_component(id)`
  - `amount` `NUMERIC(12,2)` NOT NULL
  - `active` `BOOLEAN` NOT NULL default `TRUE`
  - `effective_from` `DATE` NOT NULL

### `payroll_detail`

- Tenant-scoped: `true`
- Columns:
  - `payroll_id` `UUID` NOT NULL references `payroll(id)`
  - `payroll_component_id` `UUID` NOT NULL references `payroll_component(id)`
  - `amount` `NUMERIC(12,2)` NOT NULL
