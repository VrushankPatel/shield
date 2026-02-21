#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${PROJECT_ROOT}/docker-compose.yml"

ENV_FILE="${PROJECT_ROOT}/prod.env"
BACKUP_DIR=""
ASSUME_YES="false"

usage() {
  cat <<EOF
Usage: ./ops/restore.sh --backup-dir <path> [--env-file <path>] [--yes]

Restores PostgreSQL from postgres.dump and Redis from redis-data.tgz (if present).
The script stops app and redis containers during restore and starts them again afterwards.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --env-file)
      ENV_FILE="$2"
      shift 2
      ;;
    --backup-dir)
      BACKUP_DIR="$2"
      shift 2
      ;;
    --yes)
      ASSUME_YES="true"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ -z "$BACKUP_DIR" ]]; then
  echo "--backup-dir is required." >&2
  usage
  exit 1
fi

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Environment file not found: $ENV_FILE" >&2
  exit 1
fi

if [[ ! -f "${BACKUP_DIR}/postgres.dump" ]]; then
  echo "postgres.dump not found in backup directory: $BACKUP_DIR" >&2
  exit 1
fi

if [[ "$ASSUME_YES" != "true" ]]; then
  echo "This will replace current PostgreSQL data and optionally Redis data."
  read -r -p "Continue restore? (yes/no): " ANSWER
  if [[ "$ANSWER" != "yes" ]]; then
    echo "Restore cancelled."
    exit 0
  fi
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

POSTGRES_DB="${POSTGRES_DB:-shield}"
POSTGRES_USER="${POSTGRES_USER:-shield}"

echo "Stopping app and redis services before restore..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" stop app redis || true

echo "Recreating PostgreSQL database '${POSTGRES_DB}'..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  psql -U "$POSTGRES_USER" -d postgres -v ON_ERROR_STOP=1 \
  -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='${POSTGRES_DB}' AND pid <> pg_backend_pid();" \
  -c "DROP DATABASE IF EXISTS \"${POSTGRES_DB}\";" \
  -c "CREATE DATABASE \"${POSTGRES_DB}\";"

echo "Restoring PostgreSQL dump..."
cat "${BACKUP_DIR}/postgres.dump" | \
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
    pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --no-owner --no-privileges --clean --if-exists

if [[ -f "${BACKUP_DIR}/redis-data.tgz" ]]; then
  echo "Restoring Redis data..."
  rm -rf "${PROJECT_ROOT}/db_files/redis"
  mkdir -p "${PROJECT_ROOT}/db_files"
  tar -C "${PROJECT_ROOT}/db_files" -xzf "${BACKUP_DIR}/redis-data.tgz"
else
  echo "redis-data.tgz not found in backup directory; skipping Redis restore."
fi

echo "Starting redis and app services..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d redis app

echo "Restore completed from backup: $BACKUP_DIR"
