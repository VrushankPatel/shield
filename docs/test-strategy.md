# Test Strategy

## 1. Unit Tests
- Framework: JUnit 5 + Mockito
- Focus:
  - auth service credential paths
  - notification dispatch behavior (enabled/disabled)
  - announcement publish audience filtering
  - phase-2 module service behavior (helpdesk/emergency/document/staff/payroll/utility/marketplace)
  - expanded staff/payroll lifecycle behavior (attendance summary/date-range, process/approve/summarize payroll)
  - expanded utility and marketplace query/report behavior (consumption report, current logs, listing search/type)
  - phase-3 module service behavior (configuration/settings/files)
  - billing payment-gateway service behavior (initiation, verification, callbacks, idempotency)
  - analytics service behavior (templates, dashboards, KPI calculations)
  - validation and exception paths

## 2. Integration Tests
- Framework: Spring Boot Test + Testcontainers + RestAssured
- Database: singleton PostgreSQL container
- Focus:
  - Flyway migrations applied on fresh database
  - full auth -> JWT -> secured endpoint lifecycle
  - tenant isolation behavior
  - real-life flow scenarios (announcements, helpdesk, SOS, staff/payroll, utility, marketplace, analytics)
  - expanded phase-2 flow coverage for new staff/payroll/utility/marketplace endpoints
  - configuration/settings governance flows and file upload/download flows
  - payment gateway initiation/verification/callback flows
  - observability flows (`audit-logs`, `system-logs`, `api-request-logs`)

## 3. Security Tests
- Unauthorized access returns 401
- Role-based access checks are enforced in Spring Security config
- Token expiration/refresh behavior covered in service tests and integration flows

## 4. Execution Commands
- Unit tests only:
  ```bash
  mvn test -DskipITs
  ```
- Full suite:
  ```bash
  mvn verify
  ```

## 5. Coverage
- JaCoCo report generated during `verify`
- Coverage reports published to Codecov from CI (when `CODECOV_TOKEN` is configured)
- Target trend: 80%+ coverage over incremental milestones
