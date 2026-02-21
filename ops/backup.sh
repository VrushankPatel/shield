#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${PROJECT_ROOT}/docker-compose.yml"

ENV_FILE="${PROJECT_ROOT}/prod.env"
OUTPUT_DIR="${PROJECT_ROOT}/backups"

usage() {
  cat <<EOF
Usage: ./ops/backup.sh [--env-file <path>] [--output-dir <path>]

Creates a timestamped backup directory containing:
- postgres.dump (pg_dump custom format)
- redis-data.tgz (snapshot of db_files/redis when present)
- CHECKSUMS.sha256
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --env-file)
      ENV_FILE="$2"
      shift 2
      ;;
    --output-dir)
      OUTPUT_DIR="$2"
      shift 2
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

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Environment file not found: $ENV_FILE" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

POSTGRES_DB="${POSTGRES_DB:-shield}"
POSTGRES_USER="${POSTGRES_USER:-shield}"

TIMESTAMP="$(date -u +%Y%m%dT%H%M%SZ)"
TARGET_DIR="${OUTPUT_DIR}/${TIMESTAMP}"
mkdir -p "$TARGET_DIR"

echo "Creating PostgreSQL backup for database '${POSTGRES_DB}'..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --format=custom --no-owner --no-privileges \
  > "${TARGET_DIR}/postgres.dump"

echo "Capturing Redis data snapshot..."
if [[ -d "${PROJECT_ROOT}/db_files/redis" ]]; then
  tar -C "${PROJECT_ROOT}/db_files" -czf "${TARGET_DIR}/redis-data.tgz" redis
else
  echo "Redis data directory not found at db_files/redis; skipping redis snapshot."
fi

(
  cd "$TARGET_DIR"
  if ls postgres.dump redis-data.tgz >/dev/null 2>&1; then
    sha256sum postgres.dump redis-data.tgz > CHECKSUMS.sha256 2>/dev/null || true
  elif [[ -f postgres.dump ]]; then
    sha256sum postgres.dump > CHECKSUMS.sha256 2>/dev/null || true
  fi
)

echo "Backup completed: $TARGET_DIR"
