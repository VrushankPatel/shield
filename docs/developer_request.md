# Developer Inputs Required

This document lists operational inputs required to run SHIELD in production safely.

## 1. Environment File Policy
- Keep `dev.env` and `prod.env` in repo as templates only.
- Do not commit real credentials.
- For production, inject real values through secure environment management.

Load env variables:

```bash
set -a && source prod.env && set +a
```

## 2. Mandatory Runtime Secrets
- `JWT_SECRET`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## 3. Platform Root Account Behavior
No manual seed password is required.

Behavior:
- On first startup, if root password is missing, SHIELD generates one and logs it once.
- Root login id is fixed as `root`.
- First root login requires password change (`/api/v1/platform/root/change-password`).
- Root login lockout is controlled by:
  - `ROOT_LOCKOUT_MAX_FAILED_ATTEMPTS`
  - `ROOT_LOCKOUT_DURATION_MINUTES`

## 4. CI/CD Secrets (GitHub Actions)
- `CODECOV_TOKEN` for Codecov upload
- `SONAR_TOKEN` for SonarCloud analysis

Set these under GitHub repository:
- `Settings -> Secrets and variables -> Actions`

Optional GitHub Actions repository variables for Sonar targeting:
- `SONAR_PROJECT_KEY` (default: `VrushankPatel_shield`)
- `SONAR_ORGANIZATION` (default: `vrushankpatel`)

## 5. Container Registry Publishing
GHCR publish uses GitHub Actions `GITHUB_TOKEN` with `packages:write` permission.
No extra registry secret is needed in standard GitHub-hosted workflows.

## 6. Email Integration Inputs (Optional)
To enable SMTP email notifications:
- `NOTIFICATION_EMAIL_ENABLED=true`
- `NOTIFICATION_EMAIL_FROM`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`

### Gmail App Password Steps
1. Open [Google Account](https://myaccount.google.com/) and sign in.
2. Open `Security`.
3. Enable `2-Step Verification`.
4. Open `App passwords`.
5. Select `Mail` and device `Other`.
6. Enter name `SHIELD SMTP` and generate.
7. Use generated 16-character app password as `SPRING_MAIL_PASSWORD`.

Notes:
- Do not use normal Gmail password.
- Keep app password only in secret stores and environment variables.

## 7. Payment/Webhook Secrets (Optional)
- `PAYMENT_WEBHOOK_PROVIDER_SECRETS`

Example format:
- `STRIPE=whsec_abc,RAZORPAY=rzp_secret`

## 8. SMS/WhatsApp Placeholder Integrations
Current implementation supports placeholder toggles/providers:
- `NOTIFICATION_SMS_ENABLED`
- `NOTIFICATION_SMS_PROVIDER`
- `NOTIFICATION_WHATSAPP_ENABLED`
- `NOTIFICATION_WHATSAPP_PROVIDER`

Both channels currently use dummy provider implementations until real integrations are configured.
