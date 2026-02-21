# Database Model

## Single SQL Model File
The complete database model is maintained in a single SQL reference file:

- `docs/database-model.sql`

This file is generated from the ordered Flyway migrations under:

- `src/main/resources/db/migration/V*.sql`

## How The Model Is Written
1. Schema changes are introduced as immutable Flyway migrations.
2. Migrations are applied in version order (`V1`, `V2`, ..., `V25`).
3. The consolidated model file is regenerated from those migrations.

## Regenerate The Consolidated SQL
```bash
{
  echo "-- SHIELD consolidated database model";
  echo "-- Generated from Flyway migrations in src/main/resources/db/migration";
  echo "-- Generated at $(date -u '+%Y-%m-%dT%H:%M:%SZ')";
  echo;
  for f in $(ls src/main/resources/db/migration/V*.sql | sort -V); do
    echo "-- ===========================================================================";
    echo "-- Source: ${f}";
    echo "-- ===========================================================================";
    cat "$f";
    echo;
  done
} > docs/database-model.sql
```

## Model-Driven Additions
For generated module tables, source model JSON files are under `db/model/` and can be turned into Flyway migrations via:

- `scripts/generate_db_artifacts.py`

Flyway migrations remain the authoritative runtime schema input.
