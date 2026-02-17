# Development Rules

## 1. Architecture Boundaries
- Keep modular monolith boundaries strict by package (`module/<domain>`).
- Avoid cross-module repository calls unless explicitly justified.
- Shared concerns live only in `common`, `security`, `tenant`, and `audit`.

## 2. API Contract Discipline
- Treat `src/main/resources/openapi.yml` as the source contract.
- Any endpoint behavior change must update contract + tests in the same PR.
- Keep base path `/api/v1` and standardized response wrappers.

## 3. Multi-Tenancy Rules
- Tenant-owned tables must include `tenant_id UUID NOT NULL`.
- Tenant context must come from JWT claims only.
- Never bypass tenant filtering in service/repository logic.

## 4. Security and Secrets
- JWT-only authentication (stateless).
- RBAC is mandatory for protected endpoints.
- Never commit credentials, tokens, private keys, or secrets.
- Use env vars and CI secrets for all sensitive values.

## 5. Database and Migration Rules
- Schema changes only via Flyway under `db/migration`.
- For generated domain tables, update the source model file first (for example `db/model/phase2_schema.json`, `db/model/phase3_schema.json`), then regenerate artifacts with `scripts/generate_db_artifacts.py`.
- Migrations are immutable once merged.
- Include indexes/constraints for new query paths.

## 6. Soft Delete and Auditing
- Use soft delete (`deleted=true`) for business deletions.
- Use optimistic locking (`version`) for mutable entities.
- Record critical operations in `audit_log`.

## 7. Testing and Quality Gates
- Add/modify tests with every behavior change.
- Keep unit tests fast and isolated.
- Integration tests should use Testcontainers.
- Coverage target is 80%+ over time.

## 8. Git and Delivery
- Commit in small milestones with descriptive messages.
- Push milestone commits immediately.
- Keep `main` always buildable.
- CI must pass before release or tag.

## 9. Logging and Observability
- Include correlation ID (`X-Correlation-Id`) on every request.
- Use structured JSON logs in runtime environments.
- Maintain actuator health and Prometheus metrics endpoints.
