# Architecture

## Style
SHIELD is implemented as a modular monolith with explicit package boundaries and future microservice extraction in mind.

## Runtime Stack
- Spring Boot 3.2.x
- Spring Security (JWT + RBAC)
- Spring Data JPA + Hibernate
- PostgreSQL + Flyway
- OpenAPI (springdoc)
- SMTP email via Spring Mail

## Module Boundaries
Current modules:
- auth
- tenant
- unit
- user
- billing
- accounting
- visitor
- asset
- complaint
- amenities
- meeting
- announcement
- notification
- helpdesk
- emergency
- document
- staff
- payroll
- utility
- marketplace
- analytics
- config
- file

Cross-cutting modules:
- common
- security
- tenant (context/filter)
- audit

## Multi-Tenant Model
- Single PostgreSQL database
- `tenant_id` column in tenant-owned tables
- Tenant extracted from JWT claims
- Hibernate tenant filter enabled during transactional service execution

## Request Flow (secured endpoint)
1. Correlation ID filter injects request id
2. JWT filter authenticates token and principal
3. Tenant context filter stores tenant id in thread context
4. API request logging filter persists request/response metadata
5. Service transaction activates tenant filter
6. Repository queries are tenant-scoped
7. Response is wrapped in standard API response envelope

## Non-functional Design Choices
- Soft delete via `deleted` flag
- Optimistic locking via `version` column
- Audit log for critical operations
- Pagination/sorting on list endpoints
- Database schema source models under `db/model/phase2_schema.json`, `db/model/phase3_schema.json`, `db/model/phase4_schema.json`, `db/model/phase5_schema.json`, `db/model/phase6_schema.json`, `db/model/phase7_schema.json`, `db/model/phase8_schema.json`, `db/model/phase10_schema.json`
