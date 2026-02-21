# SHIELD Backend
[![CI](https://github.com/VrushankPatel/shield/actions/workflows/ci.yml/badge.svg)](https://github.com/VrushankPatel/shield/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/VrushankPatel/shield/graph/badge.svg)](https://codecov.io/gh/VrushankPatel/shield)
[![GHCR](https://img.shields.io/badge/GHCR-shield-blue?logo=github)](https://github.com/VrushankPatel/shield/pkgs/container/shield)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=VrushankPatel_shield&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=VrushankPatel_shield)

SHIELD stands for **Smart Housing Infrastructure and Entry Log Digitalization**.
It is a multi-tenant Spring Boot backend for residential society operations.

## Stack
- Java 17
- Spring Boot 3.2.x
- Spring Security + JWT + RBAC
- PostgreSQL + Flyway
- Spring Data JPA
- Springdoc OpenAPI
- Testcontainers + RestAssured + JUnit 5

## Architecture
- Modular monolith with strict module boundaries
- Single database multi-tenancy (`tenant_id` isolation)
- Tenant context from JWT claims
- Stateless auth with access/refresh JWT tokens

## Platform Root Flow
A dedicated platform root account is used for first-time onboarding.

- Root login id is fixed: `root`
- If root password is missing at startup, SHIELD generates a strong password and logs it once
- First root login requires password change
- Root password change requires: email, mobile, new password, confirm password
- After password change, old root tokens are invalidated automatically (token version bump)
- Root can create society + tenant admin using `POST /api/v1/platform/societies`

## API and Database Docs
- OpenAPI source: `src/main/resources/openapi.yml`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Runtime OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- API summary: `docs/api-spec.md`
- Consolidated DB SQL model: `docs/database-model.sql`
- DB model guide: `docs/database-model.md`
- Deployment guide: `docs/deployment.md`
- Dev inputs/secrets: `docs/developer_request.md`

## Local Run
1. Start dependencies:
```bash
docker compose up -d postgres redis
```

2. Run application:
```bash
mvn spring-boot:run
```

## One-Click HA Topology
`run.sh` can generate and launch multi-instance app topology with HAProxy or NGINX:

```bash
./run.sh --instances 4 --proxy haproxy
./run.sh --instances 2 --proxy nginx
```

Generated files are placed under `system_topologies/generated/`.
Persistent DB/cache data stays under `db_files/`.

## Build and Test
- Unit tests:
```bash
mvn test
```

- Full test suite with integration tests and coverage:
```bash
mvn verify
```

- Build fat jar:
```bash
mvn clean package
```

Artifact:
- `target/shield-1.0.0.jar`

## Mobile Client Note
Frontend/mobile implementation is intentionally out of scope in this repository.
The backend is built as API-first and secured for React Native or any other client.
