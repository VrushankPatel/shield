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

## 4. Required Environment Variables
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

For bootstrap and full integration input list, see `docs/developer_request.md`.

## 5. CI/CD Pipeline
GitHub Actions workflow in `.github/workflows/ci.yml`:
- Build + tests (unit + integration)
- Coverage artifact upload
- Docker image build
- Docker image publish to GitHub Container Registry (GHCR) on push events (configured branches + tags)

## 6. GHCR Image Publishing
The pipeline publishes:
- `ghcr.io/<github-owner>/shield:<project.version>`
- `ghcr.io/<github-owner>/shield:<git-sha>`
- `ghcr.io/<github-owner>/shield:latest`

Authentication uses GitHub Actions `GITHUB_TOKEN` with `packages:write` permission.
