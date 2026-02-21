# Deployment Guide

## 1. Build Artifact
```bash
mvn clean package
```
Output:
- `target/shield-1.0.0.jar`

## 2. Docker Image
```bash
docker build -t shield-api:latest .
```

## 3. Local Container Stack
```bash
docker compose up -d
```
Services:
- app: `localhost:8080`
- postgres: `localhost:5432`
- redis: `localhost:6379`

## 4. One-Click Multi-Instance Topology
Use `run.sh` to generate and run an HA setup with either HAProxy or NGINX:

```bash
./run.sh --instances 4 --proxy haproxy
./run.sh --instances 2 --proxy nginx
```

What `run.sh` does:
- Creates per-topology folders under `system_topologies/generated/`:
  - `System4NodesHaProxy`
  - `System2NodesNginx`
- Generates:
  - `docker-compose.yml`
  - `haproxy.cfg` or `nginx.conf`
- Starts the generated stack with `docker compose up -d --build`.

Stop a generated topology:

```bash
./run.sh --instances 4 --proxy haproxy --down
```

Generate files only (without starting containers):

```bash
./run.sh --instances 4 --proxy haproxy --generate-only
```

Versioned reference examples:
- `system_topologies/examples/System2NodesHaProxy`
- `system_topologies/examples/System2NodesNginx`

Persistent storage paths:
- PostgreSQL: `db_files/postgres/`
- Redis AOF: `db_files/redis/`

## 5. First Login Hardening
- Platform root login id is fixed as `root`.
- If no root password exists at startup, SHIELD generates one and logs it once.
- Immediately rotate that password through `POST /api/v1/platform/root/change-password`.
- Old root JWT sessions become invalid automatically after password change.

These directories are mounted into containers and survive container recreation/restarts.

## 5. Required Environment Variables
Core:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`

Email notifications (phase-2 optional):
- `NOTIFICATION_EMAIL_ENABLED`
- `NOTIFICATION_EMAIL_FROM`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`

Payment webhooks (phase-4 optional but recommended for production):
- `PAYMENT_WEBHOOK_PROVIDER_SECRETS` (example: `STRIPE=whsec_abc,RAZORPAY=rzp_secret`)

OTP/SMS and WhatsApp placeholders:
- `LOGIN_OTP_TTL_MINUTES`
- `LOGIN_OTP_MAX_ATTEMPTS`
- `NOTIFICATION_SMS_ENABLED`
- `NOTIFICATION_SMS_PROVIDER`
- `NOTIFICATION_WHATSAPP_ENABLED`
- `NOTIFICATION_WHATSAPP_PROVIDER`

For bootstrap and full integration input list, see `docs/developer_request.md`.

## 6. CI/CD Pipeline
GitHub Actions workflow in `.github/workflows/ci.yml`:
- Build + tests (unit + integration)
- Coverage artifact upload
- Docker image build
- Docker image publish to GitHub Container Registry (GHCR) on push events (configured branches + tags)

## 7. GHCR Image Publishing
The pipeline publishes:
- `ghcr.io/<github-owner>/shield:<project.version>`
- `ghcr.io/<github-owner>/shield:<git-sha>`
- `ghcr.io/<github-owner>/shield:latest`

Authentication uses GitHub Actions `GITHUB_TOKEN` with `packages:write` permission.
