# SHIELD Pending Milestones (Updated 2026-02-21)

## Status Legend
- `COMPLETE`: implemented and verified by tests/docs.
- `IN_PROGRESS`: partially delivered; remaining tasks listed.
- `PENDING`: intentionally deferred.

## Milestone P1 - Authentication and Session Hardening
Status: `COMPLETE`

Delivered:
- User login lockout and cooldown policy (`USER_LOCKOUT_MAX_FAILED_ATTEMPTS`, `USER_LOCKOUT_DURATION_MINUTES`).
- Login failure telemetry persisted on `users` (`failed_login_attempts`, `locked_until`, login metadata columns).
- Suspicious login audit event when login IP changes.
- Unit tests for lockout and suspicious-login behaviors.
- Integration test for lockout window and recovery flow.

## Milestone P2 - File and API Surface Hardening
Status: `COMPLETE`

Delivered:
- File content-type allowlist and max-size policy.
- Malware scan extension point (`FileMalwareScanner`) with logging placeholder implementation.
- Strict file id/name normalization and validation.
- Security headers + explicit CORS policy in `SecurityConfig`.
- Integration tests for disallowed file types/sizes and CORS/security header behavior.

## Milestone P3 - Notification and External Integration Readiness
Status: `PENDING`

Pending by design (documented in `docs/developer_request.md`):
- Real email/mobile OTP verification provider for root flows.
- Real WhatsApp provider integration.
- Real malware scanning engine adapter.
- Real payment provider deep integration flows beyond current placeholder/verification paths.

## Milestone P4 - Operability and Reliability
Status: `COMPLETE`

Delivered:
- Backup script: `ops/backup.sh`.
- Restore script: `ops/restore.sh`.
- Deployment runbook updates for backup/restore, capacity guidance, rollout and rollback.

## Milestone P5 - Quality Gates and Performance Baseline
Status: `COMPLETE`

Delivered:
- JaCoCo coverage check gates in Maven verify lifecycle.
- k6 smoke baseline script (`scripts/performance/k6-smoke.js`).
- k6 authenticated business-flow baseline script (`scripts/performance/k6-authenticated-flow.js`).
- CI job (`performance-baseline`) on main push with downloadable artifact.
- OpenAPI drift check automation via integration test (`OpenApiContractDriftIT`) that verifies
  contract/runtime mapping parity against `src/main/resources/openapi.yml`.

## Milestone P6 - Security Policy Consolidation
Status: `COMPLETE`

Delivered:
- Centralized password policy service and enforcement in:
  - Auth register/reset/change password flows
  - Root onboarding admin password flow
  - User create/bulk import flow
  - Bootstrap admin creation flow
- Password policy variables wired into `application.yml`, `dev.env`, and `prod.env`.
- Unit and integration tests for password-policy enforcement.
- Security policy baseline documented in `docs/security-policy.md`.

## Next Recommended Order
1. P3 external integration implementation phase
2. Capacity benchmarking and horizontal scaling SLO validation on target infra
3. Secrets rotation automation and regular penetration test cadence
