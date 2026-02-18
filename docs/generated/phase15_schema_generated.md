# Generated Schema

This file is generated from `db/model/phase15_schema.json`.

## Tables
### `amenity_time_slot`

- Tenant-scoped: `true`
- Columns:
  - `amenity_id` `UUID` NOT NULL references `amenity(id)`
  - `slot_name` `VARCHAR(100)` NOT NULL
  - `start_time` `TIME` NOT NULL
  - `end_time` `TIME` NOT NULL
  - `active` `BOOLEAN` NOT NULL default `TRUE`

### `amenity_pricing`

- Tenant-scoped: `true`
- Columns:
  - `amenity_id` `UUID` NOT NULL references `amenity(id)`
  - `time_slot_id` `UUID` NOT NULL references `amenity_time_slot(id)`
  - `day_type` `VARCHAR(50)` NOT NULL
  - `base_price` `NUMERIC(12,2)` NOT NULL
  - `peak_hour` `BOOLEAN` NOT NULL default `FALSE`
  - `peak_hour_multiplier` `NUMERIC(5,2)` NULL

### `amenity_booking_rule`

- Tenant-scoped: `true`
- Columns:
  - `amenity_id` `UUID` NOT NULL references `amenity(id)`
  - `rule_type` `VARCHAR(100)` NOT NULL
  - `rule_value` `VARCHAR(255)` NOT NULL
  - `active` `BOOLEAN` NOT NULL default `TRUE`

### `amenity_cancellation_policy`

- Tenant-scoped: `true`
- Columns:
  - `amenity_id` `UUID` NOT NULL references `amenity(id)`
  - `days_before_booking` `INTEGER` NOT NULL
  - `refund_percentage` `NUMERIC(6,2)` NOT NULL

### `meeting_agenda`

- Tenant-scoped: `true`
- Columns:
  - `meeting_id` `UUID` NOT NULL references `meeting(id)`
  - `agenda_item` `VARCHAR(255)` NOT NULL
  - `description` `VARCHAR(2000)` NULL
  - `display_order` `INTEGER` NOT NULL
  - `presenter` `UUID` NULL references `users(id)`
  - `estimated_duration` `INTEGER` NULL

### `meeting_attendee`

- Tenant-scoped: `true`
- Columns:
  - `meeting_id` `UUID` NOT NULL references `meeting(id)`
  - `user_id` `UUID` NOT NULL references `users(id)`
  - `invitation_sent_at` `TIMESTAMP` NULL
  - `rsvp_status` `VARCHAR(50)` NOT NULL default `'PENDING'`
  - `attendance_status` `VARCHAR(50)` NULL
  - `joined_at` `TIMESTAMP` NULL
  - `left_at` `TIMESTAMP` NULL

### `meeting_minutes_record`

- Tenant-scoped: `true`
- Columns:
  - `meeting_id` `UUID` NOT NULL references `meeting(id)`
  - `minutes_content` `TEXT` NOT NULL
  - `summary` `VARCHAR(4000)` NULL
  - `ai_generated_summary` `VARCHAR(4000)` NULL
  - `prepared_by` `UUID` NULL references `users(id)`
  - `approved_by` `UUID` NULL references `users(id)`
  - `approval_date` `DATE` NULL
  - `document_url` `VARCHAR(1000)` NULL

### `meeting_resolution`

- Tenant-scoped: `true`
- Columns:
  - `meeting_id` `UUID` NOT NULL references `meeting(id)`
  - `resolution_number` `VARCHAR(100)` NOT NULL
  - `resolution_text` `TEXT` NOT NULL
  - `proposed_by` `UUID` NULL references `users(id)`
  - `seconded_by` `UUID` NULL references `users(id)`
  - `status` `VARCHAR(50)` NOT NULL
  - `votes_for` `INTEGER` NOT NULL default `0`
  - `votes_against` `INTEGER` NOT NULL default `0`
  - `votes_abstain` `INTEGER` NOT NULL default `0`

### `meeting_vote`

- Tenant-scoped: `true`
- Columns:
  - `resolution_id` `UUID` NOT NULL references `meeting_resolution(id)`
  - `user_id` `UUID` NOT NULL references `users(id)`
  - `vote` `VARCHAR(30)` NOT NULL
  - `voted_at` `TIMESTAMP` NOT NULL default `CURRENT_TIMESTAMP`

### `meeting_action_item`

- Tenant-scoped: `true`
- Columns:
  - `meeting_id` `UUID` NOT NULL references `meeting(id)`
  - `action_description` `VARCHAR(2000)` NOT NULL
  - `assigned_to` `UUID` NULL references `users(id)`
  - `due_date` `DATE` NULL
  - `priority` `VARCHAR(20)` NULL
  - `status` `VARCHAR(50)` NOT NULL default `'PENDING'`
  - `completion_date` `DATE` NULL

### `meeting_reminder`

- Tenant-scoped: `true`
- Columns:
  - `meeting_id` `UUID` NOT NULL references `meeting(id)`
  - `reminder_type` `VARCHAR(50)` NOT NULL
  - `sent_at` `TIMESTAMP` NOT NULL default `CURRENT_TIMESTAMP`
