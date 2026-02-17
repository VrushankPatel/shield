# Test Strategy

## 1. Unit Tests
- Framework: JUnit 5 + Mockito
- Focus:
  - auth service credential paths
  - notification dispatch behavior (enabled/disabled)
  - announcement publish audience filtering
  - phase-2 module service behavior (helpdesk/emergency/document)
  - validation and exception paths

## 2. Integration Tests
- Framework: Spring Boot Test + Testcontainers + RestAssured
- Database: singleton PostgreSQL container
- Focus:
  - Flyway migrations applied on fresh database
  - full auth -> JWT -> secured endpoint lifecycle
  - tenant isolation behavior

## 3. Security Tests
- Unauthorized access returns 401
- Role-based access checks are enforced in Spring Security config
- Token expiration/refresh behavior covered in service tests and integration flows

## 4. Execution Commands
- Unit tests only:
  ```bash
  mvn test -DskipITs
  ```
- Full suite:
  ```bash
  mvn verify
  ```

## 5. Coverage
- JaCoCo report generated during `verify`
- Target trend: 80%+ coverage over incremental milestones
