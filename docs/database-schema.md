# Database Schema

PostgreSQL is the system of record.

## Tenant strategy
- Single database.
- `tenant_id UUID NOT NULL` on tenant-owned tables.
- Hibernate tenant filter enabled per request from JWT claims.

## Migration policy
- Flyway migrations under `src/main/resources/db/migration`.
- Immutable versioned SQL scripts.
- Backward-compatible migrations only.
