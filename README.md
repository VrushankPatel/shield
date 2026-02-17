# SHIELD Backend

Smart Housing Infrastructure and Entry Log Digitalization (`shield`) is a multi-tenant Spring Boot REST API platform for residential society management.

## Tech Stack
- Java 17 (local installed JDK)
- Spring Boot 3.2.x
- Spring Security + JWT
- PostgreSQL + Flyway
- Springdoc OpenAPI
- Testcontainers + RestAssured
- Docker

## Repository Layout
```text
.
├── docs/
├── src/
│   ├── main/
│   │   ├── java/com/shield/
│   │   └── resources/
│   └── test/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Development Rules
Development conventions and quality gates are maintained in `docs/development-rules.md`.

## Documentation
- `docs/architecture.md`
- `docs/database-schema.md`
- `docs/api-spec.md`
- `docs/deployment.md`
- `docs/test-strategy.md`
- `docs/developer_request.md`

## Quick Start
1. Start dependencies:
   ```bash
   docker compose up -d postgres
   ```
2. Run API:
   ```bash
   mvn spring-boot:run
   ```
3. Swagger UI:
   - [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Build
```bash
mvn clean package
```
Artifact:
- `target/society-management-api-1.0.0.jar`

## CI/CD
GitHub Actions workflow builds, tests, and prepares Docker image. Artifact publishing is configured for GitLab Artifactory via Maven `deploy` and repository secrets.
