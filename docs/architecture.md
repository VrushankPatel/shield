# Architecture

## Style
Modular monolith designed for later microservice extraction.

## Core principles
- Multi-tenant single PostgreSQL database with strict tenant isolation.
- OpenAPI-first contract under `src/main/resources/openapi.yml`.
- Domain modules isolated by package boundaries.
- Service + repository + controller layering per module.
- Shared cross-cutting concerns in `common`, `security`, and `tenant` packages.

## Planned modules
- auth
- tenant
- unit
- user
- billing
- accounting
- visitor
- asset
- complaint
- amenities
- meeting
- notification
- common
