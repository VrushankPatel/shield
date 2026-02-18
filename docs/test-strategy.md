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
  - billing management and payment operations behavior (billing cycles, invoices, reminders, late-fee rules, cash/cheque/refund flows)
  - accounting and treasury behavior (account heads/funds/vendors/expenses, budget-vs-actual aggregation, financial report calculations)
  - visitor management behavior (visitor registry, pass lifecycle, QR verify, entry/exit logs, domestic help mapping, blacklist checks, delivery logs)
  - asset and complaint expansion behavior (asset categories, complaint lifecycle/state transitions, work-order transitions, preventive maintenance execution, depreciation calculations)
  - analytics service behavior (templates, dashboards, KPI calculations)
  - validation and exception paths

## 2. Integration Tests
- Framework: Spring Boot Test + Testcontainers + RestAssured
- Database: singleton PostgreSQL container
- Focus:
  - Flyway migrations applied on fresh database
  - full auth -> JWT -> secured endpoint lifecycle
  - identity lifecycle flows (register, verify-email, forgot/reset/change password)
  - KYC + move-record + parking + digital ID operational lifecycle
  - tenant isolation behavior
  - real-life flow scenarios (announcements, helpdesk, SOS, staff/payroll, utility, marketplace, analytics)
  - expanded phase-2 flow coverage for new staff/payroll/utility/marketplace endpoints
  - configuration/settings governance flows and file upload/download flows
  - payment gateway initiation/verification/callback flows
  - M3 billing lifecycle flow (cycle -> invoice -> reminder -> payment -> refund)
  - M4 accounting lifecycle flow (account head -> fund category -> vendor -> budget -> ledger/expense -> vendor payment -> budget-vs-actual/report endpoints)
  - M5 visitor lifecycle flow (visitor -> pass -> entry/exit -> currently-inside) plus domestic-help/blacklist/delivery subflows
  - M6 asset and complaint lifecycle flow (asset category -> asset -> complaint assign/resolve/close -> work order start/complete -> preventive maintenance execute -> depreciation calculate/report)
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
