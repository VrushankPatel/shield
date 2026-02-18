# SHIELD Full-Spec Milestones

This tracker is the single source of truth for implementation progress against the full specification.

## Delivery Rules
- Every milestone must produce runnable functionality (API + service + persistence where needed).
- Every milestone must include tests (unit + integration/e2e where applicable).
- Every milestone must end with:
  - OpenAPI update
  - docs update
  - commit + push

## Current Status Summary
- Baseline and major phase-2/3 modules are implemented.
- Remaining work is to close full-spec gaps (advanced endpoint sets and production-grade integrations).

## Milestone Plan

### M1. Identity & Access Completion
Status: `COMPLETED`

Deliverables:
- Complete IAM endpoint parity:
  - `/users/unit/{unitId}`, `/users/role/{role}`, `/users/{id}/roles`, `/users/{id}/roles/{roleId}`, `/users/{id}/permissions`
  - `/users/bulk-import`, `/users/export`
  - `/units/block/{block}`, `/units/available`, `/units/{id}/members`, `/units/{id}/history`
  - `/roles/*`, `/permissions`, `/roles/{id}/permissions`, `/roles/{id}/permissions/{permissionId}`
- Strengthen OTP login flow for provider-ready extension (already added baseline).

Data/Model:
- Add role-permission and user additional role entities/repositories if missing.
- Keep model-first strategy for new DB tables/columns.

Testing:
- Unit tests for role-permission resolution and authorization behavior.
- Integration tests for cross-tenant role isolation and permission checks.

Exit Criteria:
- IAM endpoints in OpenAPI and working in integration tests.

### M2. Communication Module Completion
Status: `COMPLETE`

Deliverables:
- Announcement attachments:
  - `/announcements/{id}/attachments`, `/announcements/attachments/{attachmentId}`
- Polls:
  - `/polls/*`, `/polls/{id}/activate`, `/polls/{id}/deactivate`, `/polls/{id}/vote`, `/polls/{id}/results`
- Newsletters:
  - `/newsletters/*`, `/newsletters/{id}/publish`, `/newsletters/year/{year}`, `/newsletters/{id}/download`
- Notification improvements:
  - `/notifications/send-bulk`, `/notifications/{id}/mark-read`, `/notifications/mark-all-read`, `/notifications/unread-count`, delete endpoints.

Data/Model:
- Add missing tables for poll options/votes, newsletter artifacts, and attachment metadata.

Testing:
- E2E flow: publish announcement + read receipt + poll voting + newsletter publish.

Exit Criteria:
- Full module-2 endpoint coverage in OpenAPI and tests.

### M3. Billing & Payments Completion
Status: `COMPLETE`

Deliverables:
- Billing cycles, maintenance charges, special assessments, invoices, reminders, late-fee rules.
- Extended payment endpoints:
  - `/payments/invoice/{invoiceId}`, `/payments/unit/{unitId}`, `/payments/{id}/receipt`, `/payments/cash`, `/payments/cheque`, `/payments/{id}/refund`.

Data/Model:
- Invoice and billing-cycle entities with status transitions and constraints.

Testing:
- E2E billing lifecycle:
  - cycle -> invoice generation -> reminder -> payment -> reconciliation -> refund path.

Exit Criteria:
- Full module-3 endpoint parity and integration flow coverage.

### M4. Accounting & Treasury Completion
Status: `PENDING`

Deliverables:
- `/account-heads/*`, `/fund-categories/*`, `/ledger-entries/*`, `/expenses/*`, `/vendors/*`, `/vendor-payments/*`, `/budgets/*`
- Financial reports APIs:
  - income statement, balance sheet, cash flow, trial balance, fund summary, export endpoint.

Data/Model:
- Account hierarchy and fund-category persistence with report query support.

Testing:
- Unit tests for aggregation logic.
- Integration tests for posting entries and report correctness.

Exit Criteria:
- Full module-4 endpoint and reporting parity.

### M5. Visitor Management Completion
Status: `PENDING`

Deliverables:
- `/visitors/*`, `/visitor-passes/*`, `/visitor-logs/*`, `/domestic-help/*`, `/blacklist/*`, `/delivery-logs/*`
- QR verify and active/currently-inside behavior.

Data/Model:
- Add missing visitor submodule entities and indices.

Testing:
- E2E flow: pre-approve pass -> entry -> exit -> audit visibility.

Exit Criteria:
- Full module-5 endpoint coverage with isolation tests.

### M6. Asset & Complaint Completion
Status: `PENDING`

Deliverables:
- Asset categories, complaint comments, work orders, preventive maintenance, depreciation APIs.
- SLA-breach and complaint analytics endpoints.

Data/Model:
- Work-order and maintenance schedule tables + state transitions.

Testing:
- E2E: complaint creation -> assign -> work order -> resolve/close.

Exit Criteria:
- Full module-6 parity in OpenAPI and tests.

### M7. Amenities & Meeting Completion
Status: `PENDING`

Deliverables:
- Amenities:
  - time slots, pricing, booking rules, cancellation policy, full booking workflow endpoints.
- Meetings:
  - agenda, attendees, RSVP, minutes approvals, resolutions/votes, action-items, reminders.

Data/Model:
- Meeting governance entities and amenity pricing/rules entities.

Testing:
- E2E: meeting lifecycle + voting; amenity availability + booking + approval.

Exit Criteria:
- Full modules 7 and 8 parity.

### M8. Staff & Payroll Completion
Status: `PARTIAL`

Deliverables:
- Add remaining staff endpoints:
  - export/status variants and leave management, payroll components, salary structure, payslip endpoints.

Data/Model:
- Staff leave + salary structure + payroll details completeness.

Testing:
- E2E payroll month process with attendance impact and approval.

Exit Criteria:
- Full module-9 parity.

### M9. Helpdesk + Emergency + Documents Completion
Status: `PARTIAL`

Deliverables:
- Helpdesk full closure/rating/stats and attachment variants.
- Emergency full set:
  - fire drills, safety equipment, inspections.
- Documents:
  - search/tags/expiring/access-log/date-range endpoints.

Testing:
- Integration tests for SLA paths, emergency records, and document access logging.

Exit Criteria:
- Full modules 10, 11, and 12 parity.

### M10. Marketplace + Utility Completion
Status: `PARTIAL`

Deliverables:
- Marketplace categories/listings/inquiries/carpool full parity.
- Utility expansion:
  - diesel generators + logs, water level chart data, electricity consumption report variants.

Testing:
- E2E marketplace inquiry + listing transitions.
- E2E utility log and summary paths.

Exit Criteria:
- Full modules 13 and 14 parity.

### M11. Notifications, Config, Files, Integrations
Status: `PARTIAL`

Deliverables:
- Config/settings parity hardening.
- File APIs completion hardening.
- Production adapters:
  - SMS provider integration
  - WhatsApp provider integration (current dummy placeholder already implemented)
  - finalize payment provider adapter(s) and signature docs.

Testing:
- Integration tests with mocked provider adapters.

Exit Criteria:
- Full modules 17, 18, 19 + pending integrations completed.

### M12. Quality Gate & Release Hardening
Status: `PENDING`

Deliverables:
- Raise unit coverage to >= 80% with branch-sensitive tests.
- Add/refresh performance test scripts and document expected baselines.
- Security hardening checks for auth, RBAC, tenant isolation, and webhook validation.
- Final docs alignment (`README`, architecture, API, deployment, test strategy).

Exit Criteria:
- `mvn verify` green, CI green, coverage target met, docs consistent.

## Execution Order
Primary order:
1. M1
2. M2
3. M3
4. M4
5. M5
6. M6
7. M7
8. M8
9. M9
10. M10
11. M11
12. M12
