# Deployment Guide

## 1. Build Artifact
```bash
mvn clean package
```
Output:
- `target/shield-1.0.0.jar`

## 2. Environment Files
Two env templates are provided in repo root:
- `dev.env`
- `prod.env`

Use placeholders in repo, and inject real secrets at runtime.

Load environment variables into shell:

```bash
set -a && source prod.env && set +a
```

## 3. Standard Docker Compose Deployment
Start full stack (app + postgres + redis):

```bash
docker compose --env-file prod.env up -d --build
```

Stop stack:

```bash
docker compose --env-file prod.env down
```

Endpoints:
- app: `http://localhost:8080`
- postgres: `localhost:5432`
- redis: `localhost:6379`

Persistent storage paths (host mounted):
- PostgreSQL: `db_files/postgres/`
- Redis AOF: `db_files/redis/`

## 4. One-Click Multi-Instance Topology
Use `run.sh` for load-balanced setups with HAProxy or NGINX:

```bash
./run.sh --instances 4 --proxy haproxy --env-file prod.env
./run.sh --instances 2 --proxy nginx --env-file prod.env
```

Generate topology files only:

```bash
./run.sh --instances 4 --proxy haproxy --env-file prod.env --generate-only
```

Stop generated topology:

```bash
./run.sh --instances 4 --proxy haproxy --env-file prod.env --down
```

Generated files are placed under:
- `system_topologies/generated/System4NodesHaProxy/`
- `system_topologies/generated/System2NodesNginx/`

## 5. Required Runtime Variables
Mandatory:
- `JWT_SECRET`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

User auth hardening:
- `USER_LOCKOUT_MAX_FAILED_ATTEMPTS`
- `USER_LOCKOUT_DURATION_MINUTES`

Root account hardening:
- `ROOT_LOCKOUT_MAX_FAILED_ATTEMPTS`
- `ROOT_LOCKOUT_DURATION_MINUTES`

Password policy:
- `PASSWORD_POLICY_MIN_LENGTH`
- `PASSWORD_POLICY_MAX_LENGTH`
- `PASSWORD_POLICY_REQUIRE_UPPER`
- `PASSWORD_POLICY_REQUIRE_LOWER`
- `PASSWORD_POLICY_REQUIRE_DIGIT`
- `PASSWORD_POLICY_REQUIRE_SPECIAL`

Swagger/OpenAPI exposure:
- `SWAGGER_API_DOCS_ENABLED` (set `false` in production)
- `SWAGGER_UI_ENABLED` (set `false` in production)

API/browser security surface:
- `CORS_ALLOWED_ORIGINS`
- `CORS_ALLOWED_METHODS`
- `CORS_ALLOWED_HEADERS`
- `CORS_EXPOSED_HEADERS`
- `CORS_ALLOW_CREDENTIALS`
- `SECURITY_HSTS_ENABLED`

File ingestion policy:
- `SHIELD_FILES_MAX_SIZE_BYTES`
- `SHIELD_FILES_ALLOWED_CONTENT_TYPES`
- `SHIELD_FILES_MALWARE_SCAN_ENABLED`

Email integration:
- `NOTIFICATION_EMAIL_ENABLED`
- `NOTIFICATION_EMAIL_FROM`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`

Payment webhook verification:
- `PAYMENT_WEBHOOK_PROVIDER_SECRETS`

See `docs/developer_request.md` for full operator checklist.

## 6. Root Onboarding Security
- Platform root login id is fixed as `root`.
- If no root password exists at startup, SHIELD generates a strong password and writes it once to `ROOT_BOOTSTRAP_CREDENTIAL_FILE`.
- First login forces root password change.
- Root password change invalidates older root sessions.
- Root login now applies configurable lockout after repeated failed attempts.
- User login APIs apply configurable lockout and suspicious-login audit telemetry.

## 7. Backup and Restore Runbook
Backup (PostgreSQL dump + Redis snapshot):

```bash
./ops/backup.sh --env-file prod.env --output-dir ./backups
```

Restore from backup directory:

```bash
./ops/restore.sh --env-file prod.env --backup-dir ./backups/<timestamp> --yes
```

Notes:
- Backups operate on docker-compose services from `docker-compose.yml`.
- Redis restore is optional and skipped if `redis-data.tgz` is missing.
- Use immutable backup folders and store off-host copies.

## 8. Capacity Planning (Generated Topologies)
- `2` app instances + proxy: suitable for pilot societies and UAT traffic.
- `4` app instances + proxy: recommended default production footprint.
- `8` app instances + proxy: high-density societies or multi-society single-cluster hosting.

Guidance:
- Keep one PostgreSQL primary per deployment stack.
- Keep Redis persistence enabled (`appendonly yes`).
- Increase DB resources before increasing app replicas if DB CPU exceeds sustained `70%`.

## 9. Rollout and Rollback
Rollout:
1. Build and publish image from `main`.
2. Pull new image and run `docker compose --env-file prod.env up -d --build`.
3. Verify `/actuator/health`, `/actuator/prometheus`, login and one business flow.

Rollback:
1. Re-deploy previous known-good image tag.
2. If schema changed, restore latest verified backup with `ops/restore.sh`.
3. Re-run smoke checks and confirm error rate returns to baseline.

## 10. CI/CD and Registry Publishing
GitHub Actions workflow (`.github/workflows/ci.yml`) runs:
- Build
- Unit + integration tests
- Coverage upload (Codecov)
- Coverage threshold checks (JaCoCo `check`)
- Docker image build and push to GHCR
- SonarCloud analysis (when `SONAR_TOKEN` is configured)
- k6 smoke + authenticated baselines on `main` push (performance artifact)

Download JaCoCo coverage artifact using GitHub CLI:

```bash
gh run list --workflow ci.yml --limit 5
gh run download <run-id> -n jacoco-report
```

Download performance baseline artifact:

```bash
gh run download <run-id> -n performance-baseline
```

Published image tags:
- `ghcr.io/<owner>/shield:<project.version>`
- `ghcr.io/<owner>/shield:<git-sha>`
- `ghcr.io/<owner>/shield:latest`

Required GitHub Actions secrets:
- `CODECOV_TOKEN`
- `SONAR_TOKEN` (if SonarCloud is enabled)
