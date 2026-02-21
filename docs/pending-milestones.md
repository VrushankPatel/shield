# SHIELD Pending Milestones (Post-Endpoint and Security Patch)

## Milestone P1 - Authentication and Session Hardening
### Goal
Close remaining auth-security gaps for non-root users and standardize session controls.

### Deliverables
- Implement user login lockout and cooldown policy (`AuthService`) with audit events.
- Add suspicious login telemetry events (IP/user-agent mismatch, repeated failures).
- Add refresh-token reuse detection for non-root auth flows where applicable.
- Add integration tests for lockout and unlock workflows.
- Add unit tests for lockout counters and cooldown edge cases.

### Exit Criteria
- Unauthorized brute-force attempts are throttled and lockout is enforced.
- Regression tests pass in CI.
- Audit trail confirms lockout events and unlock behavior.

## Milestone P2 - File and API Surface Hardening
### Goal
Secure upload and request edge behavior for production deployments.

### Deliverables
- Enforce upload content-type allowlist and max-size policy in `FileStorageService`.
- Add extensible malware-scan hook interface with logging fallback implementation.
- Tighten CORS and security headers by environment in `SecurityConfig`.
- Add integration tests for rejected file types/sizes and allowed uploads.

### Exit Criteria
- Disallowed file uploads are blocked with deterministic errors.
- Security headers and CORS are verifiably applied in integration tests.

## Milestone P3 - Notification and External Integration Readiness
### Goal
Prepare real provider wiring without breaking current placeholder behavior.

### Deliverables
- Keep WhatsApp sender interface-based with dummy logger as default.
- Add email provider strategy wiring (SMTP/provider adapters) behind config flags.
- Document all external secrets needed in `docs/developer_request.md`.
- Add provider contract tests with fake/stub transports.

### Exit Criteria
- Default local mode works without secrets.
- Provider-specific mode can be enabled only through env configuration.

## Milestone P4 - Operability and Reliability
### Goal
Make production operations repeatable and failure-safe.

### Deliverables
- Backup/restore scripts and runbook for `db_files/postgres` and `db_files/redis`.
- Capacity planning guide for generated topologies (2/4/8 instance variants).
- Health/SLO runbook covering error budget, rollout, and rollback procedure.
- Add readiness/liveness operational checks to deployment docs.

### Exit Criteria
- Operators can perform backup + restore from docs/scripts only.
- Deployment playbook defines clear rollback criteria and commands.

## Milestone P5 - Quality Gates and Performance Baseline
### Goal
Lock quality and performance targets into CI.

### Deliverables
- Add strict JaCoCo threshold checks (`jacoco:check`) in CI.
- Add OpenAPI drift check against controllers/spec generation.
- Add baseline load test scenario (k6 or JMeter) for critical API flows.
- Capture baseline throughput/latency report artifact per main build.

### Exit Criteria
- CI fails on coverage or contract drift regression.
- Baseline performance report is generated and preserved in pipeline artifacts.

## Recommended Execution Order
1. P1 Authentication and Session Hardening
2. P2 File and API Surface Hardening
3. P4 Operability and Reliability
4. P3 Notification and External Integration Readiness
5. P5 Quality Gates and Performance Baseline
