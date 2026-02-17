# API Specification

## Base path
`/api/v1`

## Documentation sources
- Contract file: `src/main/resources/openapi.yml`
- Runtime docs: `/v3/api-docs`
- Swagger UI: `/swagger-ui.html`

## Security
- JWT bearer token for all endpoints except auth endpoints.
- Role-based access rules enforced in Spring Security.
