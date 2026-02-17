# API Specification

## Base Path
`/api/v1`

## Contract Source of Truth
- OpenAPI contract file: `src/main/resources/openapi.yml`
- Runtime docs: `/v3/api-docs`
- Swagger UI: `/swagger-ui.html`

## Security Model
- JWT bearer auth for all endpoints except `/auth/login` and `/auth/refresh`
- RBAC enforced via Spring Security and method-level security

## Endpoint Groups
Phase 1:
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

Phase 2:
- Announcements: `/announcements/*`
- Notifications: `/notifications/*`
- Notification preferences: `/notification-preferences/*`
- Helpdesk: `/helpdesk-categories/*`, `/helpdesk-tickets/*`
- Emergency: `/emergency-contacts/*`, `/sos-alerts/*`
- Documents: `/document-categories/*`, `/documents/*`
- Staff: `/staff/*`, `/staff-attendance/*`
- Payroll: `/payroll/*`
- Utility monitoring: `/water-tanks/*`, `/water-level-logs/*`, `/electricity-meters/*`, `/electricity-readings/*`
- Marketplace: `/marketplace-categories/*`, `/marketplace-listings/*`, `/marketplace-inquiries/*`

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
