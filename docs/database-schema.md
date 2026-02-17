# Database Schema

## Engine
PostgreSQL 15+

## Migration Strategy
- Flyway SQL migrations under `src/main/resources/db/migration`
- `V1__init_schema.sql`: phase-1 baseline schema
- `V2__announcement_and_notification.sql`: announcements + notification/email tables
- `V3__phase2_generated_modules.sql`: generated phase-2 helpdesk/emergency/document tables
- Generator source model: `db/model/phase2_schema.json`
- Generator script: `scripts/generate_db_artifacts.py`

## Core Tables
Phase 1:
- `tenant`
- `unit`
- `users`
- `maintenance_bill`
- `payment`
- `ledger_entry`
- `visitor_pass`
- `asset`
- `complaint`
- `amenity`
- `amenity_booking`
- `meeting`
- `audit_log`

Phase 2:
- `announcement`
- `notification_preference`
- `notification_email_log`
- `helpdesk_category`
- `helpdesk_ticket`
- `helpdesk_comment`
- `emergency_contact`
- `sos_alert`
- `document_category`
- `document`

## Common Columns
Most domain tables include:
- `id UUID`
- `created_at`
- `updated_at`
- `version` (optimistic locking)
- `deleted` (soft delete)

Tenant-owned tables additionally include:
- `tenant_id UUID NOT NULL`

## Tenant Isolation
- Tenant id is obtained from JWT claims.
- Hibernate filter (`tenantFilter`) scopes tenant-aware entity queries.
