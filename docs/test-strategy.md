# Test Strategy

## 1. Unit Tests
- Framework: JUnit 5 + Mockito
- Focus:
  - auth service credential paths
  - validation and exception paths
  - core business logic in services

## 2. Integration Tests
- Framework: Spring Boot Test + Testcontainers + RestAssured
- Database: ephemeral PostgreSQL container
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
- Full suite (requires Docker):
  ```bash
  mvn verify
  ```

## 5. Coverage
- JaCoCo report generated during `verify`
- Target trend: 80%+ coverage over incremental milestones
