# TODO

Primary milestone tracker:
- `docs/milestones.md`

## Pending Integrations
- [ ] Configure Gmail SMTP app password and set runtime secrets.
  - Status: On hold by request.
  - Reference: `docs/developer_request.md` (section `Detailed Steps: Generate Gmail App Password`).

## Phase-2 Continuation
- [x] **M2**: Announcement attachments (Phase 2)
- [x] **M2**: Polls module (Phase 2)
- [x] **M2**: Newsletters module (Phase 2)
- [x] **M2**: Complete Notification API (read tracking, bulk send)
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
- [x] Production payment provider integration (webhook signature verification + SDK adapter)
- [x] OTP/SMS login flow (dummy SMS sender baseline + pluggable interface)
- [x] WhatsApp notification placeholder (interface + dummy logging implementation)
- [ ] Production SMS provider integration
- [ ] Production WhatsApp provider integration

## Phase-5 In Progress
- [x] Staff attendance API expansion (`/staff/designation/{designation}`, `/staff-attendance/date/{date}`, `/staff-attendance/date-range`, `/staff-attendance/summary`)
- [x] Payroll lifecycle API expansion (`/payroll/process`, `/payroll/{id}/approve`, `/payroll/month/{month}/year/{year}`, `/payroll/staff/{staffId}`, `/payroll/summary`)
- [x] Utility query API expansion (`/water-level-logs/current`, `/water-level-logs/date-range`, `/electricity-meters/type/{type}`, `/electricity-readings/date-range`, `/electricity-readings/consumption-report`)
- [x] Marketplace query API expansion (`/marketplace-listings/type/{type}`, `/marketplace-listings/search`)

## Phase-6 Identity Extension
- [x] Auth lifecycle expansion (`register`, `verify-email`, `forgot/reset/change password`)
- [x] KYC module baseline (`/kyc/*`)
- [x] Move record workflow module (`/move-records/*`)
- [x] Parking slot management module (`/parking-slots/*`)
- [x] Digital ID card module (`/digital-id-cards/*`)
