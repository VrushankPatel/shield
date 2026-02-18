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
- Identity extension:
- Auth lifecycle: `/auth/register`, `/auth/forgot-password`, `/auth/reset-password`, `/auth/change-password`, `/auth/verify-email/{token}`
- OTP auth: `/auth/login/otp/send`, `/auth/login/otp/verify`
  - Role/permission management: `/roles/*`, `/permissions`, `/users/{id}/roles/*`, `/users/{id}/permissions`
  - User filters and bulk operations: `/users/unit/{unitId}`, `/users/role/{role}`, `/users/bulk-import`, `/users/export`
  - Unit extended queries: `/units/block/{block}`, `/units/available`, `/units/{id}/members`, `/units/{id}/history`
  - KYC: `/kyc/*`
  - Move records: `/move-records/*`
  - Parking: `/parking-slots/*`
  - Digital IDs: `/digital-id-cards/*`

- Phase 2:
  - Announcements: `/announcements/*` (with attachments `/announcements/{id}/attachments`)
  - Polls: `/polls/*`, `/polls/{id}/vote`, `/polls/{id}/results`
  - Newsletters: `/newsletters/*`, `/newsletters/year/{year}`, `/newsletters/{id}/publish`
  - Notifications: `/notifications/*` (send, bulk-send, mark-read/all, unread-count, delete)
  - Notification preferences: `/notification-preferences/*`
  - WhatsApp sender placeholder is available in code (`WhatsappNotificationSender`) with a logging dummy implementation.
- Helpdesk: `/helpdesk-categories/*`, `/helpdesk-tickets/*`
- Emergency: `/emergency-contacts/*`, `/sos-alerts/*`
- Documents: `/document-categories/*`, `/documents/*`
- Staff: `/staff/*`, `/staff-attendance/*`
- Payroll: `/payroll/*`
- Utility monitoring: `/water-tanks/*`, `/water-level-logs/*`, `/electricity-meters/*`, `/electricity-readings/*`
- Marketplace: `/marketplace-categories/*`, `/marketplace-listings/*`, `/marketplace-inquiries/*`
- Analytics reports/templates: `/report-templates/*`, `/scheduled-reports/*`
- Analytics dashboards: `/analytics-dashboards/*`
- Analytics insights: `/analytics/*`
- Audit logs: `/audit-logs/*`
- System logs: `/system-logs/*`
- API request logs: `/api-request-logs/*`
- Configuration: `/config/*`, `/settings/*`
- File management: `/files/*`
- Payment gateway lifecycle: `/payments/initiate`, `/payments/verify`, `/payments/callback`, `/payments/webhook/{provider}`, `/payments/transaction/{transactionRef}`
- Billing and payments expansion:
  - Billing cycles: `/billing-cycles/*`, `/billing-cycles/{id}/publish`, `/billing-cycles/{id}/close`, `/billing-cycles/current`, `/billing-cycles/year/{year}`
  - Maintenance charges: `/maintenance-charges/*`, `/maintenance-charges/generate`, `/maintenance-charges/cycle/{cycleId}`, `/maintenance-charges/unit/{unitId}`
  - Special assessments: `/special-assessments/*`, `/special-assessments/active`
  - Invoices: `/invoices/*`, `/invoices/generate`, `/invoices/bulk-generate`, `/invoices/unit/{unitId}`, `/invoices/cycle/{cycleId}`, `/invoices/status/{status}`, `/invoices/{id}/download`, `/invoices/defaulters`, `/invoices/outstanding`
  - Payment reminders: `/payment-reminders/*`, `/payment-reminders/send`, `/payment-reminders/schedule`, `/payment-reminders/invoice/{invoiceId}`
  - Late fee rules: `/late-fee-rules/*`, `/late-fee-rules/{id}/activate`, `/late-fee-rules/{id}/deactivate`
  - Extended payment operations: `/payments/invoice/{invoiceId}`, `/payments/unit/{unitId}`, `/payments/{id}/receipt`, `/payments/cash`, `/payments/cheque`, `/payments/{id}/refund`
- Expanded operations:
  - Staff/payroll: `/staff/designation/{designation}`, `/staff-attendance/date/{date}`, `/staff-attendance/date-range`, `/staff-attendance/summary`, `/payroll/process`, `/payroll/{id}/approve`, `/payroll/month/{month}/year/{year}`, `/payroll/staff/{staffId}`, `/payroll/summary`
  - Utility monitoring: `/water-level-logs/current`, `/water-level-logs/date-range`, `/electricity-meters/type/{type}`, `/electricity-readings/date-range`, `/electricity-readings/consumption-report`
  - Marketplace: `/marketplace-listings/type/{type}`, `/marketplace-listings/search`

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
