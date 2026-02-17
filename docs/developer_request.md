# Developer Input Requests

This file lists required developer-provided values for integrations and secure deployment.

## Required for local/prod setup
- `JWT_SECRET`: Strong signing secret for JWT.
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## Required for GitHub Actions + GitLab Artifactory publish
- `GITLAB_MAVEN_REPOSITORY_URL`
- `GITLAB_MAVEN_USERNAME`
- `GITLAB_MAVEN_TOKEN`

## Optional integrations (when modules are enabled)
- Payment gateway keys (Razorpay/Stripe/etc.)
- SMS provider credentials (Twilio/AWS SNS)
- Email provider credentials (SendGrid/AWS SES)
- WhatsApp provider credentials
- File storage credentials (S3/GCS/Azure)

## Notes
- Never commit credentials in source control.
- Store secrets in GitHub repo secrets and deployment environment variables.
