# Developer Inputs Required

This file lists values that must be provided by the developer/team for deployment and external integrations.

## 1. Mandatory Runtime Secrets
- `JWT_SECRET`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## 2. Platform Root (Initial Security)
No manual root password input is required.

Behavior:
- On first startup, if the platform root password is missing, SHIELD generates a strong password and logs it.
- Login id is fixed: `root`
- First login requires immediate password change through `/api/v1/platform/root/change-password`.

## 3. CI/CD Secrets (GitHub)
- `CODECOV_TOKEN` for Codecov upload
- `SONAR_TOKEN` for SonarCloud (if enabled)

## 4. GHCR Publishing
GHCR publish uses GitHub Actions `GITHUB_TOKEN` with `packages:write` permission.
No additional registry secret is required for normal GitHub-hosted builds.

## 5. Email Integration (Optional)
To enable SMTP email notifications:
- `NOTIFICATION_EMAIL_ENABLED=true`
- `NOTIFICATION_EMAIL_FROM`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`

### Gmail App Password Steps
1. Open [Google Account](https://myaccount.google.com/) and sign in.
2. Go to `Security`.
3. Enable `2-Step Verification`.
4. Open `App passwords`.
5. Select app type `Mail` and device `Other`.
6. Enter name `SHIELD SMTP` and generate.
7. Use generated 16-character app password as `SPRING_MAIL_PASSWORD`.

Notes:
- Do not use your normal Gmail password.
- Keep app password only in secret stores/CI environment variables.
