# TODO

## Pending Integrations
- [ ] Configure Gmail SMTP app password and set runtime secrets.
  - Status: On hold by request.
  - Reference: `docs/developer_request.md` (section `Detailed Steps: Generate Gmail App Password`).

## Phase-2 Continuation
- [x] Announcements and notification email dispatch
- [x] Helpdesk module baseline
- [x] Emergency module baseline
- [x] Document repository baseline
- [x] Staff & Payroll module
- [x] Utility monitoring module
- [x] Marketplace module
- [x] Analytics module

## Phase-3 Continuation
- [x] Observability module (`audit-logs`, `system-logs`, `api-request-logs`)
- [x] Configuration/settings module baseline
- [x] File upload/management module baseline
- [x] Payment gateway scaffold module baseline (`/payments/initiate`, `/payments/verify`, `/payments/callback`)

## Phase-4 Continuation
- [ ] Production payment provider integration (webhook signature verification + SDK adapter)
- [ ] OTP/SMS login flow
- [ ] WhatsApp notification integration

## Phase-5 In Progress
- [x] Staff attendance API expansion (`/staff/designation/{designation}`, `/staff-attendance/date/{date}`, `/staff-attendance/date-range`, `/staff-attendance/summary`)
- [x] Payroll lifecycle API expansion (`/payroll/process`, `/payroll/{id}/approve`, `/payroll/month/{month}/year/{year}`, `/payroll/staff/{staffId}`, `/payroll/summary`)
- [x] Utility query API expansion (`/water-level-logs/current`, `/water-level-logs/date-range`, `/electricity-meters/type/{type}`, `/electricity-readings/date-range`, `/electricity-readings/consumption-report`)
- [x] Marketplace query API expansion (`/marketplace-listings/type/{type}`, `/marketplace-listings/search`)
