# Generated Schema

This file is generated from `db/model/phase3_schema.json`.

## Tables
### `staff`

- Tenant-scoped: `true`
- Columns:
  - `employee_id` `VARCHAR(50)` NOT NULL unique
  - `first_name` `VARCHAR(100)` NULL
  - `last_name` `VARCHAR(100)` NULL
  - `phone` `VARCHAR(20)` NULL
  - `email` `VARCHAR(255)` NULL
  - `designation` `VARCHAR(100)` NOT NULL
  - `date_of_joining` `DATE` NOT NULL
  - `date_of_leaving` `DATE` NULL
  - `employment_type` `VARCHAR(50)` NOT NULL
  - `basic_salary` `NUMERIC(10, 2)` NOT NULL
  - `active` `BOOLEAN` NOT NULL default `TRUE`

### `staff_attendance`

- Tenant-scoped: `true`
- Columns:
  - `staff_id` `UUID` NOT NULL references `staff(id)`
  - `attendance_date` `DATE` NOT NULL
  - `check_in_time` `TIMESTAMP` NULL
  - `check_out_time` `TIMESTAMP` NULL
  - `status` `VARCHAR(50)` NOT NULL
  - `marked_by` `UUID` NULL references `users(id)`

### `payroll`

- Tenant-scoped: `true`
- Columns:
  - `staff_id` `UUID` NOT NULL references `staff(id)`
  - `month` `INTEGER` NOT NULL
  - `year` `INTEGER` NOT NULL
  - `working_days` `INTEGER` NOT NULL
  - `present_days` `INTEGER` NOT NULL
  - `gross_salary` `NUMERIC(10, 2)` NOT NULL
  - `total_deductions` `NUMERIC(10, 2)` NOT NULL default `0`
  - `net_salary` `NUMERIC(10, 2)` NOT NULL
  - `payment_date` `DATE` NULL
  - `payment_method` `VARCHAR(50)` NULL
  - `payment_reference` `VARCHAR(255)` NULL
  - `status` `VARCHAR(50)` NOT NULL
  - `payslip_url` `VARCHAR(2000)` NULL

### `water_tank`

- Tenant-scoped: `true`
- Columns:
  - `tank_name` `VARCHAR(100)` NOT NULL
  - `tank_type` `VARCHAR(50)` NOT NULL
  - `capacity` `NUMERIC(10, 2)` NOT NULL
  - `location` `VARCHAR(255)` NULL

### `water_level_log`

- Tenant-scoped: `true`
- Columns:
  - `tank_id` `UUID` NOT NULL references `water_tank(id)`
  - `reading_time` `TIMESTAMP` NOT NULL default `CURRENT_TIMESTAMP`
  - `level_percentage` `NUMERIC(5, 2)` NOT NULL
  - `volume` `NUMERIC(10, 2)` NULL
  - `recorded_by` `UUID` NULL references `users(id)`

### `electricity_meter`

- Tenant-scoped: `true`
- Columns:
  - `meter_number` `VARCHAR(100)` NOT NULL unique
  - `meter_type` `VARCHAR(50)` NOT NULL
  - `location` `VARCHAR(255)` NULL
  - `unit_id` `UUID` NULL

### `electricity_reading`

- Tenant-scoped: `true`
- Columns:
  - `meter_id` `UUID` NOT NULL references `electricity_meter(id)`
  - `reading_date` `DATE` NOT NULL
  - `reading_value` `NUMERIC(10, 2)` NOT NULL
  - `units_consumed` `NUMERIC(10, 2)` NULL
  - `cost` `NUMERIC(10, 2)` NULL
  - `recorded_by` `UUID` NULL references `users(id)`

### `marketplace_category`

- Tenant-scoped: `true`
- Columns:
  - `category_name` `VARCHAR(100)` NOT NULL
  - `description` `VARCHAR(500)` NULL

### `marketplace_listing`

- Tenant-scoped: `true`
- Columns:
  - `listing_number` `VARCHAR(100)` NOT NULL unique
  - `category_id` `UUID` NULL references `marketplace_category(id)`
  - `listing_type` `VARCHAR(50)` NOT NULL
  - `title` `VARCHAR(255)` NOT NULL
  - `description` `TEXT` NULL
  - `price` `NUMERIC(10, 2)` NULL
  - `negotiable` `BOOLEAN` NOT NULL default `FALSE`
  - `images` `VARCHAR(2000)` NULL
  - `posted_by` `UUID` NULL references `users(id)`
  - `unit_id` `UUID` NULL
  - `status` `VARCHAR(50)` NOT NULL
  - `views_count` `INTEGER` NOT NULL default `0`
  - `expires_at` `TIMESTAMP` NULL

### `marketplace_inquiry`

- Tenant-scoped: `true`
- Columns:
  - `listing_id` `UUID` NOT NULL references `marketplace_listing(id)`
  - `inquired_by` `UUID` NULL references `users(id)`
  - `message` `VARCHAR(2000)` NULL
