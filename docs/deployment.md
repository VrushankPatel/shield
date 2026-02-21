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

Root account hardening:
- `ROOT_LOCKOUT_MAX_FAILED_ATTEMPTS`
- `ROOT_LOCKOUT_DURATION_MINUTES`

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
- If no root password exists at startup, SHIELD generates a strong password and logs it once.
- First login forces root password change.
- Root password change invalidates older root sessions.
- Root login now applies configurable lockout after repeated failed attempts.

## 7. CI/CD and Registry Publishing
GitHub Actions workflow (`.github/workflows/ci.yml`) runs:
- Build
- Unit + integration tests
- Coverage upload (Codecov)
- Docker image build and push to GHCR
- SonarCloud analysis (when `SONAR_TOKEN` is configured)
- PDF/JSON/Markdown quality artifact generation for Sonar and coverage

Download quality artifacts using GitHub CLI:

```bash
gh run list --workflow ci.yml --limit 5
gh run download <run-id> -n sonar-quality-report -n coverage-quality-report -n jacoco-report
```

Published image tags:
- `ghcr.io/<owner>/shield:<project.version>`
- `ghcr.io/<owner>/shield:<git-sha>`
- `ghcr.io/<owner>/shield:latest`

Required GitHub Actions secrets:
- `CODECOV_TOKEN`
- `SONAR_TOKEN` (if SonarCloud is enabled)
