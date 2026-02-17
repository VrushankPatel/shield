# Generated Schema

This file is generated from `db/model/phase4_schema.json`.

## Tables
### `report_template`

- Tenant-scoped: `true`
- Columns:
  - `template_name` `VARCHAR(255)` NOT NULL
  - `report_type` `VARCHAR(100)` NOT NULL
  - `description` `VARCHAR(1000)` NULL
  - `query_template` `TEXT` NULL
  - `parameters_json` `TEXT` NULL
  - `created_by` `UUID` NULL references `users(id)`
  - `system_template` `BOOLEAN` NOT NULL default `FALSE`

### `scheduled_report`

- Tenant-scoped: `true`
- Columns:
  - `template_id` `UUID` NOT NULL references `report_template(id)`
  - `report_name` `VARCHAR(255)` NOT NULL
  - `frequency` `VARCHAR(50)` NOT NULL
  - `recipients` `VARCHAR(2000)` NULL
  - `active` `BOOLEAN` NOT NULL default `TRUE`
  - `last_generated_at` `TIMESTAMP` NULL
  - `next_generation_at` `TIMESTAMP` NULL

### `analytics_dashboard`

- Tenant-scoped: `true`
- Columns:
  - `dashboard_name` `VARCHAR(255)` NOT NULL
  - `dashboard_type` `VARCHAR(100)` NOT NULL
  - `widgets_json` `TEXT` NULL
  - `created_by` `UUID` NULL references `users(id)`
  - `default_dashboard` `BOOLEAN` NOT NULL default `FALSE`
