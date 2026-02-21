# API Specification

## Base Path
- `/api/v1`

## OpenAPI Contract
- Source file: `src/main/resources/openapi.yml`
- Runtime JSON: `/v3/api-docs`
- Swagger UI: `/swagger-ui.html`

## Security Model
- JWT Bearer authentication (stateless)
- RBAC with Spring Security + method security
- Tenant context derived from JWT `tenantId` claim for tenant-scoped endpoints
- Platform root uses dedicated principal type (`ROOT`) with token version invalidation
- User refresh sessions are stored server-side (`auth_token` with `REFRESH_SESSION`)
- Refresh token rotation is enforced; reused refresh tokens are rejected
- Logout, password change, and password reset revoke active refresh sessions

## Platform Root APIs
- `POST /platform/root/login`
- `POST /platform/root/refresh`
- `POST /platform/root/change-password`
- `POST /platform/societies`

Root lifecycle behavior:
- Root login id is fixed as `root`
- First login requires password change before society onboarding
- Root password change requires `email`, `mobile`, `newPassword`, `confirmNewPassword`
- Password change increments token version, invalidating old sessions

## Core Domain APIs
- Auth: `/auth/*`
- Tenant/Unit/User/IAM: `/tenants/*`, `/units/*`, `/users/*`, `/roles/*`, `/permissions/*`
- Billing/Payments: `/billing-cycles/*`, `/maintenance-charges/*`, `/invoices/*`, `/payments/*`
- Accounting: `/account-heads/*`, `/ledger-entries/*`, `/expenses/*`, `/vendors/*`, `/reports/*`
- Visitor: `/visitors/*`, `/visitor-passes/*`, `/visitor-logs/*`, `/domestic-help/*`, `/blacklist/*`
- Asset/Complaint: `/asset-categories/*`, `/assets/*`, `/complaints/*`, `/work-orders/*`
- Amenities/Meetings: `/amenities/*`, `/amenity-bookings/*`, `/meetings/*`, `/resolutions/*`
- Communication: `/announcements/*`, `/polls/*`, `/newsletters/*`, `/notifications/*`
- Staff/Payroll: `/staff/*`, `/staff-attendance/*`, `/staff-leaves/*`, `/payroll*`
- Utility/Marketplace: `/water-*`, `/electricity-*`, `/diesel-generators/*`, `/generator-logs/*`, `/marketplace-*`, `/carpool-listings/*`
- Helpdesk/Emergency/Documents: `/helpdesk-*`, `/emergency-*`, `/sos-alerts/*`, `/documents/*`, `/document-categories/hierarchy`
- Analytics/Config/Files/Logs: `/analytics/*`, `/config/*`, `/settings/*`, `/files/*`, `/audit-logs/*`

### Communication Notes
- Announcements now include:
  - filtered listing: `/announcements/category/{category}`, `/announcements/priority/{priority}`, `/announcements/active`
  - read tracking: `POST /announcements/{id}/mark-read`
  - read visibility: `GET /announcements/{id}/read-receipts`, `GET /announcements/{id}/statistics`

### Identity Notes
- Unit and user lifecycle now include:
  - unit ownership transition: `PATCH /units/{id}/ownership`
  - user activation state transition: `PATCH /users/{id}/status`

## Mobile App Readiness
The backend is API-first and frontend-agnostic. React Native clients should use:
- short-lived access token + refresh flow
- secure storage for tokens
- refresh token replacement on every refresh response
- forced re-login on root token invalidation or credential rotation
