# Deployment

## Local
- Java 17
- PostgreSQL 15+

## Build
```bash
mvn clean package
```

## Docker
```bash
docker build -t shield-api:latest .
```

## Runtime configuration
Use environment variables for datasource, JWT secret, and integration credentials.
