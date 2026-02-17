# Deployment Guide

## 1. Build Artifact
```bash
mvn clean package
```
Output:
- `target/society-management-api-1.0.0.jar`

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
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`

For first run bootstrap, see `docs/developer_request.md`.

## 5. CI/CD Pipeline
GitHub Actions workflow in `.github/workflows/ci.yml`:
- Build + tests
- Coverage artifact
- Docker image build
- Maven deploy to GitLab Artifactory (if secrets set)

## 6. Artifact Publishing to GitLab Artifactory
Configure repository secrets:
- `GITLAB_MAVEN_REPOSITORY_URL`
- `GITLAB_MAVEN_USERNAME`
- `GITLAB_MAVEN_TOKEN`

Pipeline deploys with Maven `deploy` to server id `gitlab-maven`.
