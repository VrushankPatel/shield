# Development Rules

## Branching and commits
- Commit in small milestones with clear, imperative commit messages.
- Push milestone commits to remote immediately.

## API and contracts
- Add/modify API behavior only with OpenAPI contract updates.
- Preserve backward compatibility for public endpoints.

## Multi-tenancy
- All tenant-owned data must carry `tenant_id`.
- Never bypass tenant filtering in business APIs.

## Data and migrations
- All schema changes through Flyway migrations.
- No destructive migration in the same release unless explicitly approved.

## Security
- JWT-based auth only; no hardcoded credentials.
- Enforce RBAC at endpoint and service layers.
- Sanitize logs and never print secrets.

## Testing
- Add or update tests for every behavior change.
- Run unit + integration tests before merge.

## Coding
- Keep modules independent.
- Use DTOs between API and domain.
- Use MapStruct for mapping.
- Keep controllers thin and services focused.
