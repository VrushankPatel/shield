# SHIELD Requirements Cross-Check (2026-02-22)

## Summary
- Modular monolith backend implemented with Spring Boot + PostgreSQL + Flyway + JWT RBAC.
- OpenAPI-first contract is present at `src/main/resources/openapi.yml`.
- Runtime contract parity is enforced by integration test `OpenApiContractDriftIT`.
- Swagger/OpenAPI public exposure is now env-controlled and **disabled by default**.
- Full validation gate (`mvn clean verify`) passes on `main`.

## API Surface Status
- Controllers in modular packages: `84`
- Contract paths in OpenAPI: `530`
- HTTP operations in OpenAPI: `726`

Coverage guard:
- `OpenApiContractDriftIT` compares runtime handler mappings with `openapi.yml` and fails CI on drift.

## Module-by-Module Functional Check
Status legend:
- `COMPLETE`: implemented in controller/service/repository and covered by integration flow tests.
- `PENDING_EXTERNAL`: intentionally deferred provider integration (documented).

1. Identity/IAM (`auth`, `tenant`, `unit`, `user`, `role`, `kyc`, `move`, `parking`, `digitalid`, root platform onboarding): `COMPLETE`
2. Communication (`announcement`, `poll`, `newsletter`, `notification`): `COMPLETE`
3. Billing/Payments (`billing`, payment webhook verification paths): `COMPLETE`
4. Accounting/Treasury (`accounting`): `COMPLETE`
5. Visitor/Gate (`visitor`): `COMPLETE`
6. Asset/Complaint/Work orders (`asset`, `complaint`): `COMPLETE`
7. Amenities/Bookings (`amenities`): `COMPLETE`
8. Meetings/Governance (`meeting`): `COMPLETE`
9. Staff/Payroll (`staff`, `payroll`): `COMPLETE`
10. Helpdesk (`helpdesk`): `COMPLETE`
11. Emergency/Safety (`emergency`): `COMPLETE`
12. Document repository (`document`, `file`): `COMPLETE`
13. Marketplace (`marketplace`): `COMPLETE`
14. Utility monitoring (`utility`): `COMPLETE`
15. Analytics/Reports (`analytics`): `COMPLETE`
16. Operability/Observability (`audit`, logs, actuator/prometheus): `COMPLETE`

## Integration Test Coverage Check
Integration suite size: `22` end-to-end module tests:
- `AccountingTreasuryModuleIT`
- `AmenitiesMeetingFlowsIT`
- `AnalyticsFlowsIT`
- `AssetComplaintModuleIT`
- `AuthSessionSecurityIT`
- `BillingPaymentsModuleIT`
- `CommunicationModuleIT`
- `ConfigAndFilesFlowsIT`
- `FlywayMigrationIT`
- `IdentityAccessFlowsIT`
- `M9CompletionFlowsIT`
- `ObservabilityLogsIT`
- `OpenApiContractDriftIT`
- `PaymentGatewayFlowsIT`
- `PendingEndpointsCompletionIT`
- `Phase2ExpansionFlowsIT`
- `PlatformRootOnboardingIT`
- `RealLifeFlowsIT`
- `StaffPayrollCompletionIT`
- `SwaggerExposureIT`
- `TenantIsolationIT`
- `VisitorManagementModuleIT`

Additional quality gates:
- Unit tests + integration tests run in `mvn verify`.
- JaCoCo threshold enforcement runs in verify lifecycle.

## Security/Runtime Policy Check
- JWT auth with refresh-session rotation and revocation: in place.
- Root onboarding + first-login password change flow: in place.
- User/root lockout policies: in place.
- Password policy centralization and enforcement: in place.
- Swagger/OpenAPI exposure control:
  - `SWAGGER_API_DOCS_ENABLED`
  - `SWAGGER_UI_ENABLED`
  - defaults set to `false` in `application.yml`.
  - `dev.env` enables both, `prod.env` disables both.

## Intentional Pending Items
Tracked in `docs/developer_request.md`:
- Real OTP provider for root verification.
- Real WhatsApp provider.
- Real malware scanning engine.
- Full production-grade payment provider settlement/callback integrations.
