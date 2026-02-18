# Developer Request: Required Inputs

This file tracks values that must be provided by the developer/team for complete deployment and integrations.

## A. Mandatory Runtime Inputs
Provide these for every environment:
- `JWT_SECRET`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## B. First-Run Bootstrap Inputs (recommended)
Set only during first startup to create initial tenant + admin:
- `BOOTSTRAP_ENABLED=true`
- `BOOTSTRAP_TENANT_NAME`
- `BOOTSTRAP_TENANT_ADDRESS` (optional)
- `BOOTSTRAP_ADMIN_NAME` (optional, default: Shield Admin)
- `BOOTSTRAP_ADMIN_EMAIL`
- `BOOTSTRAP_ADMIN_PASSWORD`

After first successful bootstrap:
- `BOOTSTRAP_ENABLED=false`

## C. GitHub Actions -> GitLab Artifactory Publish Inputs
Add these as GitHub repository secrets:
- `GITLAB_MAVEN_REPOSITORY_URL`
- `GITLAB_MAVEN_USERNAME`
- `GITLAB_MAVEN_TOKEN`

Set with GitHub CLI:
```bash
gh secret set GITLAB_MAVEN_REPOSITORY_URL --repo VrushankPatel/shield
gh secret set GITLAB_MAVEN_USERNAME --repo VrushankPatel/shield
gh secret set GITLAB_MAVEN_TOKEN --repo VrushankPatel/shield
```

## D. Email Integration Inputs (Phase-2 Active)
For SMTP email notifications (announcements + manual notification dispatch), provide:
- `NOTIFICATION_EMAIL_ENABLED=true`
- `NOTIFICATION_EMAIL_FROM`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`

### Gmail-specific values
- `SPRING_MAIL_HOST=smtp.gmail.com`
- `SPRING_MAIL_PORT=587`
- `SPRING_MAIL_USERNAME=<your-gmail-address>`
- `SPRING_MAIL_PASSWORD=<gmail-app-password>`

Important:
- Do not use your normal Gmail account password.
- Use a Google App Password.
- Current status: on hold (tracked in `docs/todo.md`).

## E. Detailed Steps: Generate Gmail App Password
1. Open [Google Account](https://myaccount.google.com/) and sign in.
2. Go to `Security`.
3. Enable `2-Step Verification` if not already enabled.
4. After 2FA is enabled, return to `Security` and open `App passwords`.
5. In app passwords:
- Select app as `Mail` (or choose `Other` and enter `shield-backend`).
- Click `Generate`.
6. Google shows a 16-character app password.
7. Copy it immediately and store it in your secret manager.
8. Use that 16-character value as `SPRING_MAIL_PASSWORD`.

## F. GitHub CLI: set email secrets for this repo
```bash
gh secret set NOTIFICATION_EMAIL_ENABLED --repo VrushankPatel/shield
gh secret set NOTIFICATION_EMAIL_FROM --repo VrushankPatel/shield
gh secret set SPRING_MAIL_HOST --repo VrushankPatel/shield
gh secret set SPRING_MAIL_PORT --repo VrushankPatel/shield
gh secret set SPRING_MAIL_USERNAME --repo VrushankPatel/shield
gh secret set SPRING_MAIL_PASSWORD --repo VrushankPatel/shield
```

## G. Inputs Still Pending for Future Modules
- Payments provider + API/webhook credentials
- SMS/OTP provider credentials
- WhatsApp provider credentials
- External object storage credentials (optional future switch from local disk storage)

## I. File Storage Mode (Current)
- Current implementation uses local disk storage only.
- Configure storage root with `SHIELD_FILES_STORAGE_PATH` (default `./storage/files`).
- No cloud storage API keys are required for the current baseline.

## H. Security Notes
- Never commit credentials in source control.
- Keep credentials in secret manager / CI secrets / runtime env vars only.
