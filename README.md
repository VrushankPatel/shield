# SHIELD Backend
[![CI](https://github.com/VrushankPatel/shield/actions/workflows/ci.yml/badge.svg)](https://github.com/VrushankPatel/shield/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/VrushankPatel/shield/graph/badge.svg)](https://codecov.io/gh/VrushankPatel/shield)
[![GHCR](https://img.shields.io/badge/GHCR-shield-blue?logo=github)](https://github.com/VrushankPatel/shield/pkgs/container/shield)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=VrushankPatel_shield&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=VrushankPatel_shield)

SHIELD stands for **Smart Housing Infrastructure and Entry Log Digitalization**.

It is a multi-tenant Spring Boot backend for residential society operations with JWT-based RBAC, OpenAPI-first contracts, Flyway migrations, Testcontainers integration testing, and GHCR-ready CI/CD.

## Technology Stack
- Java 17+
- Spring Boot 3.2.x
- Spring Security + JWT + RBAC
- PostgreSQL + Flyway
- Spring Data JPA
- Springdoc OpenAPI
- Testcontainers + RestAssured + JUnit 5

## Key Functional Scope
- Identity and access management (tenant, unit, user, role)
- Billing, payments, accounting, and treasury records
- Visitor and gate pass workflows
- Asset, complaint, and work-order lifecycle
- Amenities and meeting governance
- Staff/payroll, utility monitoring, marketplace, helpdesk
- Platform root onboarding flow for new societies

## Project Docs
- Architecture: `docs/architecture.md`
- API spec summary: `docs/api-spec.md`
- OpenAPI source: `src/main/resources/openapi.yml`
- Database SQL model: `docs/database-model.sql`
- Database model guide: `docs/database-model.md`
- Deployment guide: `docs/deployment.md`
- Developer integration inputs: `docs/developer_request.md`
- Plan gap analysis: `docs/implementation-gap-analysis.md`
- Pending milestones: `docs/pending-milestones.md`

## Environment Strategy (`dev.env`, `prod.env`)
`dev.env` and `prod.env` are committed as **templates**. Keep real secrets in secure stores or local override files.

Load env values into your shell:

```bash
set -a && source dev.env && set +a
```

or

```bash
set -a && source prod.env && set +a
```

Spring config reads these through normal process environment variables.

## Local Development Run
1. Load development env:
```bash
set -a && source dev.env && set +a
```

2. Start local dependencies:
```bash
docker compose --env-file dev.env up -d postgres redis
```

3. Run API:
```bash
mvn spring-boot:run
```

4. Open API docs:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Docker Compose Full Stack
Run app + postgres + redis using production template values:

```bash
docker compose --env-file prod.env up -d --build
```

Persistent data directories:
- PostgreSQL: `db_files/postgres`
- Redis append-only data: `db_files/redis`

## One-Click Multi-Instance Topology
Use `run.sh` to generate and run HAProxy/NGINX balanced multi-instance topologies:

```bash
./run.sh --instances 4 --proxy haproxy --env-file prod.env
./run.sh --instances 2 --proxy nginx --env-file prod.env
```

Stop generated topology:

```bash
./run.sh --instances 4 --proxy haproxy --env-file prod.env --down
```

Generate files only:

```bash
./run.sh --instances 4 --proxy haproxy --env-file prod.env --generate-only
```

Generated artifacts are under `system_topologies/generated/`.

## Scripts
- `run.sh`: Generate and run/load-balanced deployment topologies.
- `scripts/generate_db_artifacts.py`: Generate migration/doc artifacts from DB model JSON.

## Build, Test, and Coverage
Run tests:

```bash
mvn test
```

Run full verification (unit + integration + coverage):

```bash
mvn verify
```

Build fat jar:

```bash
mvn clean package
```

Artifact:
- `target/shield-1.0.0.jar`

## CI Quality Artifacts
CI publishes JaCoCo HTML coverage reports as downloadable artifact:
- `jacoco-report`

Download from terminal with GitHub CLI:

```bash
gh run list --workflow ci.yml --limit 5
gh run download <run-id> -n jacoco-report
```

## Production Secret Wiring
Do not hardcode secrets in source control.

Runtime environment secrets (examples):
- `JWT_SECRET`
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`
- `PAYMENT_WEBHOOK_PROVIDER_SECRETS`
- `ROOT_BOOTSTRAP_CREDENTIAL_FILE` (secure path for first-run root credential output)
- `PAYMENT_WEBHOOK_REQUIRE_PROVIDER_SECRET` (recommended `true` in production)

GitHub Actions secrets:
- `CODECOV_TOKEN`
- `SONAR_TOKEN`

For GitHub repository setup:
1. Open repo `Settings` -> `Secrets and variables` -> `Actions`.
2. Add required secrets.
3. Keep `prod.env` placeholders only; inject real values at deploy runtime.

## Security Notes
- Platform root login id is fixed as `root`.
- On first startup, if missing, root password is generated once and written to `ROOT_BOOTSTRAP_CREDENTIAL_FILE`.
- First root login requires password change.
- Password change bumps root token version and invalidates old sessions.
- Root login lockout policy is configurable:
  - `ROOT_LOCKOUT_MAX_FAILED_ATTEMPTS`
  - `ROOT_LOCKOUT_DURATION_MINUTES`

## Client Scope
This repository is backend-only. Mobile/web clients are out of scope here.
