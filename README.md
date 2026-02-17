# SHIELD Backend

SHIELD stands for **Smart Housing Infrastructure and Entry Log Digitalization**.
This repository contains a **multi-tenant modular monolith** backend built with Spring Boot.

## Current Scope
### Phase 1
- JWT auth (`/auth/login`, `/auth/refresh`, `/auth/logout`)
- Tenant, Unit, User
- Billing + Payments
- Accounting Ledger
- Visitor Pass
- Asset
- Complaint
- Amenities + Bookings
- Meeting + Minutes

### Phase 2 (started)
- Announcements module (`/announcements`)
- Email notifications module (`/notifications/send`, `/notifications`, `/notifications/{id}`)
- Notification preferences (`/notification-preferences`)
- Announcement publish -> tenant audience filtered email dispatch
- Helpdesk module (`/helpdesk-categories`, `/helpdesk-tickets`)
- Emergency module (`/emergency-contacts`, `/sos-alerts`)
- Document repository module (`/document-categories`, `/documents`)
- DB model-driven migration generation (`db/model/phase2_schema.json` -> `V3__phase2_generated_modules.sql`)

Cross-cutting:
- Tenant context + Hibernate tenant filter
- RBAC with Spring Security
- Login rate limiting
- Global error handler
- Flyway migrations (`V1`, `V2`)
- Structured JSON logs + correlation id
- Actuator + Prometheus endpoint

## Tech Stack
- Java 17 (aligned with locally installed JDK)
- Spring Boot 3.2.x
- Spring Security + JWT (`jjwt`)
- Spring Data JPA + PostgreSQL
- Flyway
- Spring Mail (SMTP)
- Springdoc OpenAPI
- MapStruct + Lombok
- JUnit 5 + Mockito + Testcontainers + RestAssured
- Docker

## Repository Structure
```text
.
├── docs/
├── src/main/java/com/shield/
│   ├── common/
│   ├── security/
│   ├── tenant/
│   ├── module/
│   │   ├── auth/
│   │   ├── tenant/
│   │   ├── unit/
│   │   ├── user/
│   │   ├── billing/
│   │   ├── accounting/
│   │   ├── visitor/
│   │   ├── asset/
│   │   ├── complaint/
│   │   ├── amenities/
│   │   ├── meeting/
│   │   ├── announcement/
│   │   ├── notification/
│   │   ├── helpdesk/
│   │   ├── emergency/
│   │   └── document/
│   └── audit/
├── src/main/resources/
│   ├── db/migration/
│   ├── openapi.yml
│   └── application*.yml
├── src/test/
├── .github/workflows/ci.yml
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Prerequisites
- Java 17
- Maven 3.9+
- Docker Desktop (required for integration tests/Testcontainers)

## Local Development
### 1. Start dependencies
```bash
docker compose up -d postgres redis
```

### 2. Bootstrap first tenant + admin (first run only)
```bash
export BOOTSTRAP_ENABLED=true
export BOOTSTRAP_TENANT_NAME="Demo Society"
export BOOTSTRAP_ADMIN_EMAIL="admin@shield.local"
export BOOTSTRAP_ADMIN_PASSWORD="ChangeThis123!"
```

### 3. Optional: enable SMTP email notifications
```bash
export NOTIFICATION_EMAIL_ENABLED=true
export NOTIFICATION_EMAIL_FROM="your@gmail.com"
export SPRING_MAIL_HOST="smtp.gmail.com"
export SPRING_MAIL_PORT="587"
export SPRING_MAIL_USERNAME="your@gmail.com"
export SPRING_MAIL_PASSWORD="your-16-char-app-password"
```
Detailed Gmail setup is documented in `docs/developer_request.md`.

### 4. Run application
```bash
mvn spring-boot:run
```

### 5. OpenAPI docs
- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Build and Test
### Package fat JAR
```bash
mvn clean package
```
Artifact:
- `target/society-management-api-1.0.0.jar`

### Unit tests only
```bash
mvn test -DskipITs
```

### Full verification (includes Testcontainers integration tests)
```bash
mvn verify
```

## Docker
### Build image
```bash
docker build -t shield-api:latest .
```

### Run full stack
```bash
docker compose up -d
```

## CI/CD
GitHub Actions workflow (`.github/workflows/ci.yml`) runs:
1. Maven build + tests + coverage report
2. Docker image build
3. Maven artifact publish to GitLab Artifactory (when secrets are configured)

Required secrets for publish job:
- `GITLAB_MAVEN_REPOSITORY_URL`
- `GITLAB_MAVEN_USERNAME`
- `GITLAB_MAVEN_TOKEN`

## Documentation
- `docs/architecture.md`
- `docs/database-schema.md`
- `docs/generated/phase2_schema_generated.md`
- `docs/api-spec.md`
- `docs/deployment.md`
- `docs/test-strategy.md`
- `docs/development-rules.md`
- `docs/developer_request.md`
- `docs/todo.md`
