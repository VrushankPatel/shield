# Database Schema

## Engine
PostgreSQL 15+

## Migration Strategy
- Flyway SQL migrations under `src/main/resources/db/migration`
- `V1__init_schema.sql`: phase-1 baseline schema
- `V2__announcement_and_notification.sql`: announcements + notification/email tables
- `V3__phase2_generated_modules.sql`: generated phase-2 helpdesk/emergency/document tables
- `V4__phase2_staff_utility_marketplace_generated.sql`: generated phase-2 staff/payroll/utility/marketplace tables
- `V5__phase3_analytics_generated.sql`: generated phase-3 analytics tables
- `V6__phase4_log_observability_generated.sql`: generated phase-4 observability log tables
- `V7__phase5_config_files_generated.sql`: generated phase-5 configuration and files tables
- `V8__phase6_payment_gateway_generated.sql`: generated phase-6 payment gateway transaction tables
- `V9__phase8_identity_extensions_generated.sql`: generated phase-8 identity extension tables
- `V10__phase10_iam_rbac_generated.sql`: generated phase-10 IAM RBAC tables
- `V11__phase9_communication_completion.sql`: communication module completion tables
- `V12__phase11_billing_expansion_generated.sql`: generated phase-11 billing expansion tables
- `V13__phase11_billing_payment_alterations.sql`: payment table extensions for invoice-linked flows
- `V14__phase12_accounting_treasury_generated.sql`: generated phase-12 accounting/treasury tables
- `V15__phase12_ledger_entry_extensions.sql`: phase-12 ledger enhancements for accounting reports
- `V16__phase13_visitor_expansion_generated.sql`: generated phase-13 visitor management tables
- `V17__phase13_visitor_pass_extensions.sql`: phase-13 visitor-pass table enrichments
- `V18__phase14_asset_complaint_expansion_generated.sql`: generated phase-14 asset/complaint expansion tables
- `V19__phase14_asset_complaint_extensions.sql`: phase-14 asset/complaint baseline table enrichments
- `V20__phase15_amenities_meeting_generated.sql`: generated phase-15 amenities/meeting governance tables
- `V21__phase15_amenities_meeting_extensions.sql`: phase-15 amenities/meeting baseline table enrichments
- `V22__phase16_staff_payroll_completion_generated.sql`: generated phase-16 staff/payroll completion tables
- Generator source model: `db/model/phase2_schema.json`
- Generator source model: `db/model/phase3_schema.json`
- Generator source model: `db/model/phase4_schema.json`
- Generator source model: `db/model/phase5_schema.json`
- Generator source model: `db/model/phase6_schema.json`
- Generator source model: `db/model/phase7_schema.json`
- Generator source model: `db/model/phase8_schema.json`
- Generator source model: `db/model/phase10_schema.json`
- Generator source model: `db/model/phase11_schema.json`
- Generator source model: `db/model/phase12_schema.json`
- Generator source model: `db/model/phase13_schema.json`
- Generator source model: `db/model/phase14_schema.json`
- Generator source model: `db/model/phase15_schema.json`
- Generator source model: `db/model/phase16_schema.json`
- Generator script: `scripts/generate_db_artifacts.py`

## Core Tables
Phase 1:
- `tenant`
- `unit`
- `users`
- `maintenance_bill`
- `payment`
- `ledger_entry`
- `visitor_pass`
- `asset`
- `complaint`
- `amenity`
- `amenity_booking`
- `meeting`
- `audit_log`

Phase 2:
- `announcement`
- `notification_preference`
- `notification_email_log`
- `helpdesk_category`
- `helpdesk_ticket`
- `helpdesk_comment`
- `emergency_contact`
- `sos_alert`
- `document_category`
- `document`
- `staff`
- `staff_attendance`
- `payroll`
- `water_tank`
- `water_level_log`
- `electricity_meter`
- `electricity_reading`
- `marketplace_category`
- `marketplace_listing`
- `marketplace_inquiry`
- `report_template`
- `scheduled_report`
- `analytics_dashboard`
- `system_log`
- `api_request_log`
- `tenant_config`
- `system_setting`
- `stored_file`
- `payment_gateway_txn`
- `auth_token`
- `kyc_document`
- `move_record`
- `parking_slot`
- `digital_id_card`
- `app_role`
- `permission`
- `role_permission`
- `user_additional_role`
- `billing_cycle`
- `maintenance_charge`
- `special_assessment`
- `invoice`
- `payment_reminder`
- `late_fee_rule`
- `account_head`
- `fund_category`
- `vendor`
- `expense`
- `vendor_payment`
- `budget`
- `visitor`
- `visitor_entry_exit_log`
- `domestic_help_registry`
- `domestic_help_unit_mapping`
- `blacklist`
- `delivery_log`
- `asset_category`
- `complaint_comment`
- `work_order`
- `preventive_maintenance_schedule`
- `asset_depreciation`
- `amenity_time_slot`
- `amenity_pricing`
- `amenity_booking_rule`
- `amenity_cancellation_policy`
- `meeting_agenda`
- `meeting_attendee`
- `meeting_minutes_record`
- `meeting_resolution`
- `meeting_vote`
- `meeting_action_item`
- `meeting_reminder`
- `staff_leave`
- `payroll_component`
- `staff_salary_structure`
- `payroll_detail`

## Common Columns
Most domain tables include:
- `id UUID`
- `created_at`
- `updated_at`
- `version` (optimistic locking)
- `deleted` (soft delete)

Tenant-owned tables additionally include:
- `tenant_id UUID NOT NULL`

## Tenant Isolation
- Tenant id is obtained from JWT claims.
- Hibernate filter (`tenantFilter`) scopes tenant-aware entity queries.
