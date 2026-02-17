# API Specification

## Base Path
`/api/v1`

## Contract Source of Truth
- OpenAPI contract file: `src/main/resources/openapi.yml`
- Runtime docs: `/v3/api-docs`
- Swagger UI: `/swagger-ui.html`

## Security Model
- JWT bearer auth for all endpoints except `/auth/login` and `/auth/refresh`
- RBAC enforced via Spring Security

## Phase-1 Endpoint Groups
- Auth: `/auth/*`
- Tenant: `/tenants/*`
- Unit: `/units/*`
- User: `/users/*`
- Billing: `/billing/*`, `/payments/*`
- Accounting: `/ledger/*`
- Visitor: `/visitors/pass/*`
- Asset: `/assets/*`
- Complaint: `/complaints/*`
- Amenities: `/amenities/*`
- Meeting: `/meetings/*`

## Common Error Envelope
```json
{
  "timestamp": "2026-02-17T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/users"
}
```
