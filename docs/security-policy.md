# SHIELD Security Policy (Backend)

## Purpose
This document defines the baseline runtime security policy for SHIELD backend deployments.
External provider integrations are intentionally deferred and tracked in `docs/developer_request.md`.

## 1. Authentication and Session Policy
- Access token: short-lived JWT (`JWT_ACCESS_TOKEN_TTL_MINUTES`, default 30).
- Refresh token: long-lived JWT (`JWT_REFRESH_TOKEN_TTL_MINUTES`, default 4320).
- Refresh token rotation is mandatory:
  - every refresh consumes old session token hash
  - old refresh tokens are rejected after use
- Password change, reset, and logout revoke active refresh sessions for that principal.

## 2. Password Policy
Password policy is centralized and enforced in:
- user registration / reset / change flows
- tenant admin onboarding via root flow
- user create/bulk import flows
- bootstrap admin creation flow

Policy variables:
- `PASSWORD_POLICY_MIN_LENGTH` (default `12`)
- `PASSWORD_POLICY_MAX_LENGTH` (default `128`)
- `PASSWORD_POLICY_REQUIRE_UPPER` (default `true`)
- `PASSWORD_POLICY_REQUIRE_LOWER` (default `true`)
- `PASSWORD_POLICY_REQUIRE_DIGIT` (default `true`)
- `PASSWORD_POLICY_REQUIRE_SPECIAL` (default `true`)

## 3. Login Abuse Controls
- User login lockout:
  - `USER_LOCKOUT_MAX_FAILED_ATTEMPTS`
  - `USER_LOCKOUT_DURATION_MINUTES`
- Root login lockout:
  - `ROOT_LOCKOUT_MAX_FAILED_ATTEMPTS`
  - `ROOT_LOCKOUT_DURATION_MINUTES`
- Login endpoint rate limiting:
  - `LOGIN_RATE_LIMIT_REQUESTS`
  - `LOGIN_RATE_LIMIT_WINDOW_SECONDS`

## 4. Root Account and Onboarding Policy
- Root login id is fixed as `root`.
- If no root password exists, startup generates one and stores it in `ROOT_BOOTSTRAP_CREDENTIAL_FILE`.
- First root login requires password change.
- Root password change requires email and mobile verification calls (currently placeholder provider).
- Root token version increments on password change and invalidates prior sessions.

## 5. API and Browser Security Policy
- Explicit CORS allowlist via:
  - `CORS_ALLOWED_ORIGINS`
  - `CORS_ALLOWED_METHODS`
  - `CORS_ALLOWED_HEADERS`
  - `CORS_EXPOSED_HEADERS`
- HSTS toggle:
  - `SECURITY_HSTS_ENABLED`
- Correlation id and structured logs are required for request tracing.

## 6. File Upload Security Policy
- Max file size and content type allowlist enforced:
  - `SHIELD_FILES_MAX_SIZE_BYTES`
  - `SHIELD_FILES_ALLOWED_CONTENT_TYPES`
- Malware scan hook exists behind:
  - `SHIELD_FILES_MALWARE_SCAN_ENABLED`
- Current malware scanner is placeholder and must be replaced for production-grade scanning.

## 7. Data Safety and Recovery Policy
- Persistent volumes are required for PostgreSQL and Redis.
- Routine backup and restore procedures are provided:
  - `ops/backup.sh`
  - `ops/restore.sh`

## 8. Deferred External Integrations
Pending items (tracked, not implemented in this milestone):
- Real OTP provider for root email/mobile verification
- Real WhatsApp provider
- Real malware scanning engine
- Full production payment provider flows (settlement/webhook hardening)
