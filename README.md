# SHIELD Backend
[![CI](https://github.com/VrushankPatel/shield/actions/workflows/ci.yml/badge.svg)](https://github.com/VrushankPatel/shield/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/VrushankPatel/shield/graph/badge.svg)](https://codecov.io/gh/VrushankPatel/shield)
[![GHCR](https://img.shields.io/badge/GHCR-shield-blue?logo=github)](https://github.com/VrushankPatel/shield/pkgs/container/shield)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=VrushankPatel_shield&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=VrushankPatel_shield)

SHIELD stands for **Smart Housing Infrastructure and Entry Log Digitalization**.
This repository contains a **multi-tenant modular monolith** backend built with Spring Boot.

## Current Scope
### Phase 1
- JWT auth (`/auth/login`, `/auth/refresh`, `/auth/logout`)
- Auth lifecycle extensions (`/auth/register`, `/auth/forgot-password`, `/auth/reset-password`, `/auth/change-password`, `/auth/verify-email/{token}`)
- OTP auth flow (`/auth/login/otp/send`, `/auth/login/otp/verify`) with pluggable SMS sender (dummy logger implementation by default)
- Tenant, Unit, User
- IAM expansion:
  - Role and permission APIs (`/roles/*`, `/permissions`)
  - User role assignment and permission resolution (`/users/{id}/roles/*`, `/users/{id}/permissions`)
  - User filters/bulk/export (`/users/unit/{unitId}`, `/users/role/{role}`, `/users/bulk-import`, `/users/export`)
  - Unit filters and occupancy/member views (`/units/block/{block}`, `/units/available`, `/units/{id}/members`, `/units/{id}/history`)
- Billing + Payments
- Accounting + Treasury
- Visitor Management
- Asset
- Complaint
- Amenities + Bookings
- Meeting + Minutes
- Identity operations:
  - KYC (`/kyc/*`)
  - Move records (`/move-records/*`)
  - Parking slots (`/parking-slots/*`)
  - Digital ID cards (`/digital-id-cards/*`)

### Phase 2 (started)
- Announcements module (`/announcements`)
- Email notifications module (`/notifications/send`, `/notifications`, `/notifications/{id}`)
- Notification preferences (`/notification-preferences`)
- WhatsApp sender placeholder interface + dummy logger implementation
- Announcement publish -> tenant audience filtered email dispatch
- Helpdesk module (`/helpdesk-categories`, `/helpdesk-tickets`)
- Emergency module (`/emergency-contacts`, `/sos-alerts`)
- Document repository module (`/document-categories`, `/documents`)
- Staff and payroll module (`/staff`, `/staff-attendance`, `/payroll`)
- Utility monitoring module (`/water-tanks`, `/water-level-logs`, `/electricity-meters`, `/electricity-readings`)
- Marketplace module (`/marketplace-categories`, `/marketplace-listings`, `/marketplace-inquiries`)
- Analytics module (`/report-templates`, `/scheduled-reports`, `/analytics-dashboards`, `/analytics/*`)
- Observability log APIs (`/audit-logs`, `/system-logs`, `/api-request-logs`)
- Configuration/settings APIs (`/config/*`, `/settings/*`)
- File management APIs (`/files/*`)
- Payment gateway APIs with webhook signature validation + provider adapters (`/payments/initiate`, `/payments/verify`, `/payments/callback`, `/payments/webhook/{provider}`, `/payments/transaction/{transactionRef}`)
- Billing and payments expansion:
  - `/billing-cycles/*`
  - `/maintenance-charges/*`
  - `/special-assessments/*`
  - `/invoices/*`
  - `/payment-reminders/*`
  - `/late-fee-rules/*`
  - extended payment operations: `/payments/invoice/{invoiceId}`, `/payments/unit/{unitId}`, `/payments/{id}/receipt`, `/payments/cash`, `/payments/cheque`, `/payments/{id}/refund`
- Accounting and treasury expansion:
  - `/account-heads/*`
  - `/fund-categories/*`
  - `/ledger-entries/*`
  - `/expenses/*`
  - `/vendors/*`
  - `/vendor-payments/*`
  - `/budgets/*`
  - financial reports: `/reports/income-statement`, `/reports/balance-sheet`, `/reports/cash-flow`, `/reports/trial-balance`, `/reports/fund-summary`, `/reports/export/ca-format`
- Visitor management expansion:
  - `/visitors/*`, `/visitors/search`, `/visitors/phone/{phone}`
  - `/visitor-passes/*`, `/visitor-passes/create`, `/visitor-passes/unit/{unitId}`, `/visitor-passes/date/{date}`, `/visitor-passes/active`, `/visitor-passes/verify/{qrCode}`, `/visitor-passes/pre-approve`
  - `/visitor-logs/*`, `/visitor-logs/entry`, `/visitor-logs/exit`, `/visitor-logs/pass/{passId}`, `/visitor-logs/date-range`, `/visitor-logs/currently-inside`
  - `/domestic-help/*`, `/blacklist/*`, `/delivery-logs/*`
  - legacy compatibility path retained: `/visitors/pass/*`
- DB model-driven migration generation:
  - `db/model/phase2_schema.json` -> `V3__phase2_generated_modules.sql`
  - `db/model/phase3_schema.json` -> `V4__phase2_staff_utility_marketplace_generated.sql`
  - `db/model/phase4_schema.json` -> `V5__phase3_analytics_generated.sql`
  - `db/model/phase5_schema.json` -> `V6__phase4_log_observability_generated.sql`
  - `db/model/phase6_schema.json` -> `V7__phase5_config_files_generated.sql`
  - `db/model/phase7_schema.json` -> `V8__phase6_payment_gateway_generated.sql`
  - `db/model/phase8_schema.json` -> `V9__phase8_identity_extensions_generated.sql`
  - `db/model/phase10_schema.json` -> `V10__phase10_iam_rbac_generated.sql`
  - `db/model/phase11_schema.json` -> `V12__phase11_billing_expansion_generated.sql` (+ `V13__phase11_billing_payment_alterations.sql`)
  - `db/model/phase12_schema.json` -> `V14__phase12_accounting_treasury_generated.sql` (+ `V15__phase12_ledger_entry_extensions.sql`)
  - `db/model/phase13_schema.json` -> `V16__phase13_visitor_expansion_generated.sql` (+ `V17__phase13_visitor_pass_extensions.sql`)

Cross-cutting:
- Tenant context + Hibernate tenant filter
- RBAC with Spring Security
- Login rate limiting
- Global error handler
- Flyway migrations (`V1` to `V17`)
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
│   │   ├── role/
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
│   │   ├── document/
│   │   ├── staff/
│   │   ├── payroll/
│   │   ├── utility/
│   │   ├── marketplace/
│   │   ├── analytics/
│   │   ├── config/
│   │   └── file/
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
export SHIELD_APP_BASE_URL="http://localhost:8080"
export PAYMENT_WEBHOOK_PROVIDER_SECRETS="STRIPE=whsec_xxx,RAZORPAY=rzp_webhook_secret"
export LOGIN_OTP_TTL_MINUTES="5"
export LOGIN_OTP_MAX_ATTEMPTS="5"
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
- `target/shield-1.0.0.jar`

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
2. Coverage upload to Codecov (when `CODECOV_TOKEN` is configured)
3. Docker image build
4. GHCR image publish on `push` events (branches + tags)

Published image tags:
- `ghcr.io/<github-owner>/shield:<project.version>`
- `ghcr.io/<github-owner>/shield:<git-sha>`
- `ghcr.io/<github-owner>/shield:latest`
- `ghcr.io/<github-owner>/shield:<branch>` for branch pushes (`main`, `release/**`, `feature/**`, `codex/**`)
- `ghcr.io/<github-owner>/shield:<tag>` for Git tags (`v*`)

Required secrets:
- `CODECOV_TOKEN` (for coverage upload)

## Documentation
- `docs/architecture.md`
- `docs/database-schema.md`
- `docs/generated/phase2_schema_generated.md`
- `docs/generated/phase3_schema_generated.md`
- `docs/generated/phase4_schema_generated.md`
- `docs/generated/phase5_schema_generated.md`
- `docs/generated/phase6_schema_generated.md`
- `docs/generated/phase7_schema_generated.md`
- `docs/generated/phase8_schema_generated.md`
- `docs/generated/phase10_schema_generated.md`
- `docs/generated/phase11_schema_generated.md`
- `docs/generated/phase12_schema_generated.md`
- `docs/generated/phase13_schema_generated.md`
- `docs/api-spec.md`
- `docs/deployment.md`
- `docs/test-strategy.md`
- `docs/development-rules.md`
- `docs/developer_request.md`
- `docs/milestones.md`
- `docs/todo.md`
