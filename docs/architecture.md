# Architecture

## Style
SHIELD is implemented as a **modular monolith** with explicit package boundaries and future microservice extraction in mind.

## Runtime Stack
- Spring Boot 3.2.x
- Spring Security (JWT + RBAC)
- Spring Data JPA + Hibernate
- PostgreSQL + Flyway
- OpenAPI (springdoc)

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
- notification (placeholder)

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
4. Service transaction activates tenant filter
5. Repository queries are tenant-scoped
6. Response is wrapped in standard API response envelope

## Non-functional Design Choices
- Soft delete via `deleted` flag
- Optimistic locking via `version` column
- Audit log for critical operations
- Pagination/sorting on list endpoints
