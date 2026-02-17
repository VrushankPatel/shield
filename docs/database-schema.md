# Database Schema

## Engine
PostgreSQL 15+

## Migration Strategy
- Flyway SQL migrations under `src/main/resources/db/migration`
- `V1__init_schema.sql`: phase-1 baseline schema
- `V2__announcement_and_notification.sql`: announcements + notification/email tables

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
