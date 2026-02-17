# Developer Request: Required Inputs

This file tracks values that must be provided by the developer/team for a complete deployment and integration rollout.

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

After the first successful bootstrap, set:
- `BOOTSTRAP_ENABLED=false`

## C. GitHub Actions -> GitLab Artifactory Publish Inputs
Add these as GitHub repository secrets:
- `GITLAB_MAVEN_REPOSITORY_URL`
- `GITLAB_MAVEN_USERNAME`
- `GITLAB_MAVEN_TOKEN`

## D. Integrations Requiring Your Decision/API Keys
The following modules are planned; provide values when enabled:

### Payments
- Payment provider choice (Razorpay/Stripe/etc.)
- API key / secret / webhook secret
- Settlement account config

### SMS / OTP
- Provider selection (Twilio or AWS SNS)
- API credentials
- Sender ID / messaging profile

### Email
- Provider selection (SendGrid or AWS SES)
- API key or IAM credentials
- Verified sender domain/email

### WhatsApp Notifications
- Provider credentials and template IDs

### File Storage
- S3/GCS/Azure bucket info
- Access key/secret or IAM role config

### Optional Monitoring and Alerting
- Log sink endpoint (if external)
- Alert webhook URLs (Slack/Teams/PagerDuty)

## E. Security Notes
- Do not commit any secrets in `application*.yml`.
- Keep credentials only in secret managers / CI secrets / runtime env vars.
