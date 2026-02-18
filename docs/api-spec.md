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
- Accounting (baseline): `/ledger/*`
- Visitor (legacy): `/visitors/pass/*`
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
- Accounting and treasury expansion:
  - Account heads: `/account-heads/*`, `/account-heads/type/{type}`, `/account-heads/hierarchy`
  - Fund categories: `/fund-categories/*`, `/fund-categories/{id}/balance`, `/fund-categories/balances`
  - Ledger entries: `/ledger-entries/*`, `/ledger-entries/account/{accountHeadId}`, `/ledger-entries/fund/{fundCategoryId}`, `/ledger-entries/date-range`, `/ledger-entries/bulk`, `/ledger-entries/export`
  - Expenses: `/expenses/*`, `/expenses/{id}/approve`, `/expenses/{id}/reject`, `/expenses/pending-approval`, `/expenses/vendor/{vendorId}`, `/expenses/account/{accountHeadId}`, `/expenses/date-range`, `/expenses/export`
  - Vendors: `/vendors/*`, `/vendors/type/{type}`, `/vendors/active`, `/vendors/{id}/status`
  - Vendor payments: `/vendor-payments/*`, `/vendor-payments/vendor/{vendorId}`, `/vendor-payments/expense/{expenseId}`, `/vendor-payments/pending`
  - Budgets and reports: `/budgets/*`, `/budgets/financial-year/{year}`, `/budgets/vs-actual`, `/reports/income-statement`, `/reports/balance-sheet`, `/reports/cash-flow`, `/reports/trial-balance`, `/reports/fund-summary`, `/reports/export/ca-format`
- Visitor management expansion:
  - Visitors: `/visitors/*`, `/visitors/search`, `/visitors/phone/{phone}`
  - Visitor passes: `/visitor-passes/*`, `/visitor-passes/create`, `/visitor-passes/unit/{unitId}`, `/visitor-passes/date/{date}`, `/visitor-passes/active`, `/visitor-passes/verify/{qrCode}`, `/visitor-passes/pre-approve`
  - Visitor logs: `/visitor-logs/*`, `/visitor-logs/entry`, `/visitor-logs/exit`, `/visitor-logs/pass/{passId}`, `/visitor-logs/date-range`, `/visitor-logs/currently-inside`
  - Domestic help: `/domestic-help/*`, `/domestic-help/type/{type}`, `/domestic-help/{id}/verify`, `/domestic-help/{id}/assign-unit`, `/domestic-help/{helpId}/unit/{unitId}`
  - Blacklist: `/blacklist/*`, `/blacklist/check/{phone}`, `/blacklist/{id}/activate`, `/blacklist/{id}/deactivate`
  - Delivery logs: `/delivery-logs/*`, `/delivery-logs/unit/{unitId}`, `/delivery-logs/date-range`, `/delivery-logs/partner/{partner}`
- Asset and complaint expansion:
  - Asset categories: `/asset-categories/*`
  - Asset advanced search/export: `/assets/category/{categoryId}`, `/assets/location/{location}`, `/assets/tag/{tag}`, `/assets/verify-qr/{qrCode}`, `/assets/amc-expiring`, `/assets/warranty-expiring`, `/assets/export`
  - Complaint full lifecycle and filters: `/complaints/{id}`, `/complaints/{id}/assign`, `/complaints/{id}/resolve`, `/complaints/{id}/close`, `/complaints/{id}/reopen`, `/complaints/status/{status}`, `/complaints/priority/{priority}`, `/complaints/asset/{assetId}`, `/complaints/my-complaints`, `/complaints/assigned-to-me`, `/complaints/sla-breached`, `/complaints/statistics`
  - Complaint comments: `/complaints/{id}/comments`, `/comments/{id}`
  - Work orders: `/work-orders/*`, `/work-orders/{id}/start`, `/work-orders/{id}/complete`, `/work-orders/{id}/cancel`, `/work-orders/complaint/{complaintId}`, `/work-orders/vendor/{vendorId}`, `/work-orders/status/{status}`
  - Preventive maintenance: `/preventive-maintenance/*`, `/preventive-maintenance/asset/{assetId}`, `/preventive-maintenance/due`, `/preventive-maintenance/{id}/execute`
  - Asset depreciation: `/asset-depreciation/asset/{assetId}`, `/asset-depreciation/calculate`, `/asset-depreciation/year/{year}`, `/asset-depreciation/report`
- Amenities and meeting expansion:
  - Amenity master + activation: `/amenities/*`, `/amenities/type/{type}`, `/amenities/available`, `/amenities/{id}/activate`, `/amenities/{id}/deactivate`
  - Amenity slots/pricing: `/amenities/{id}/time-slots`, `/time-slots/{id}`, `/amenities/{id}/pricing`, `/pricing/{id}`
  - Amenity booking lifecycle: `/amenity-bookings/*`, `/amenity-bookings/check-availability`, `/amenity-bookings/{id}/approve`, `/amenity-bookings/{id}/reject`, `/amenity-bookings/{id}/cancel`, `/amenity-bookings/{id}/complete`
  - Amenity rules/policies: `/amenities/{id}/rules`, `/booking-rules/{id}`, `/amenities/{id}/cancellation-policy`, `/cancellation-policy/{id}`
  - Meeting lifecycle: `/meetings/*`, `/meetings/{id}/start`, `/meetings/{id}/end`, `/meetings/{id}/cancel`, `/meetings/upcoming`, `/meetings/past`, `/meetings/type/{type}`
  - Meeting agenda/attendees/minutes/resolutions/votes: `/meetings/{id}/agenda`, `/agenda/{id}`, `/meetings/{id}/attendees`, `/meetings/{id}/rsvp`, `/meetings/{id}/mark-attendance`, `/meetings/{id}/minutes`, `/minutes/{id}`, `/meetings/{id}/resolutions`, `/resolutions/{id}/vote`, `/resolutions/{id}/results`
  - Meeting action items/reminders: `/meetings/{id}/action-items`, `/action-items/{id}`, `/action-items/assigned-to-me`, `/action-items/pending`, `/meetings/{id}/send-reminders`, `/meetings/{id}/reminders`
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
