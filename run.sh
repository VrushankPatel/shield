#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

INSTANCES=2
PROXY="haproxy"
ACTION="up"
GENERATE_ONLY=false
ENV_FILE=""

POSTGRES_HOST_DIR="$ROOT_DIR/db_files/postgres"
REDIS_HOST_DIR="$ROOT_DIR/db_files/redis"

print_banner() {
  cat <<'BANNER'
   _____ _   _ ___ _____ _     ____
  / ____| | | |_ _| ____| |   |  _ \\
 | (___ | |_| || ||  _| | |   | | | |
  \___ \|  _  || || |___| |___| |_| |
  ____) | | | || ||_____|_____|____/
 |_____/|_| |_|___|

 Smart Housing Infrastructure and Entry Log Digitalization
BANNER
}

usage() {
  print_banner
  cat <<USAGE

Usage:
  run.sh --instances <count> --proxy <haproxy|nginx> [--env-file <path>] [--generate-only]
  run.sh --instances <count> --proxy <haproxy|nginx> [--env-file <path>] --down

Examples:
  ./run.sh --instances 4 --proxy haproxy --env-file prod.env
  ./run.sh --instances 2 --proxy nginx --env-file dev.env
  ./run.sh --instances 4 --proxy haproxy --generate-only
  /absolute/path/to/run.sh --instances 3 --proxy haproxy --env-file /absolute/path/to/prod.env

Options:
  --instances <count>   Number of API app instances (must be >= 1)
  --proxy <name>        Load balancer: haproxy or nginx
  --env-file <path>     Environment file path. Relative path resolves by:
                        1) current working directory
                        2) project root ($ROOT_DIR)
                        default: $ROOT_DIR/prod.env
  --generate-only       Generate topology and config files only (no compose up/down)
  --down                Stop and remove the generated stack
  -h, --help            Show this help and exit

What this script generates:
  - topology directory: system_topologies/generated/System<instances>Nodes<Proxy>/
  - docker-compose.yml
  - haproxy.cfg or nginx.conf

Persistence:
  - PostgreSQL data: $POSTGRES_HOST_DIR
  - Redis data:      $REDIS_HOST_DIR

USAGE
}

err() {
  echo "[ERROR] $*" >&2
}

require_value() {
  local flag="$1"
  local value="${2:-}"
  if [[ -z "$value" || "$value" == --* ]]; then
    err "Missing value for $flag"
    usage
    exit 1
  fi
}

abspath() {
  local p="$1"
  if [[ "$p" = /* ]]; then
    printf '%s\n' "$p"
    return
  fi

  local dir
  dir="$(cd "$(dirname "$p")" && pwd)"
  printf '%s/%s\n' "$dir" "$(basename "$p")"
}

resolve_env_file() {
  local raw="$1"

  if [[ "$raw" = /* ]]; then
    printf '%s\n' "$raw"
    return
  fi

  if [[ -f "$raw" ]]; then
    abspath "$raw"
    return
  fi

  if [[ -f "$ROOT_DIR/$raw" ]]; then
    abspath "$ROOT_DIR/$raw"
    return
  fi

  # Fallback for error handling path display
  printf '%s\n' "$ROOT_DIR/$raw"
}

load_env_file() {
  local env_file="$1"
  if [[ ! -f "$env_file" ]]; then
    err "Env file not found: $env_file"
    exit 1
  fi

  set -a
  # shellcheck disable=SC1090
  source "$env_file"
  set +a
}

proxy_label() {
  if [[ "$1" == "haproxy" ]]; then
    echo "HaProxy"
  else
    echo "Nginx"
  fi
}

print_failure_diagnostics() {
  local compose_project="$1"
  local postgres_container="${compose_project}-postgres-1"

  echo
  err "Stack startup failed. Diagnostics:"

  if docker ps -a --format '{{.Names}}' | grep -qx "$postgres_container"; then
    echo "--- postgres logs ($postgres_container) ---"
    docker logs --tail 120 "$postgres_container" || true
    echo "--- end postgres logs ---"

    if docker logs "$postgres_container" 2>&1 | grep -q "exists but is not empty"; then
      echo
      err "PostgreSQL initdb failed because data directory is a non-empty mount root."
      echo "Hint: script now uses PGDATA subdirectory '/var/lib/postgresql/data/pgdata' to avoid this."
      echo "If old broken files exist, clean only when safe:"
      echo "  rm -rf '$POSTGRES_HOST_DIR/pgdata'"
    fi
  else
    err "Postgres container not found ($postgres_container)."
  fi
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --instances)
      require_value "$1" "${2:-}"
      INSTANCES="$2"
      shift 2
      ;;
    --proxy)
      require_value "$1" "${2:-}"
      PROXY="$2"
      shift 2
      ;;
    --env-file)
      require_value "$1" "${2:-}"
      ENV_FILE="$(resolve_env_file "$2")"
      shift 2
      ;;
    --generate-only)
      GENERATE_ONLY=true
      shift
      ;;
    --down)
      ACTION="down"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      err "Unknown option: $1"
      usage
      exit 1
      ;;
  esac
done

if ! [[ "$INSTANCES" =~ ^[0-9]+$ ]] || (( INSTANCES < 1 )); then
  err "--instances must be a positive integer >= 1"
  exit 1
fi

case "$PROXY" in
  haproxy|nginx) ;;
  *)
    err "--proxy must be either 'haproxy' or 'nginx'"
    exit 1
    ;;
esac

if [[ -z "$ENV_FILE" ]]; then
  ENV_FILE="$ROOT_DIR/prod.env"
fi

load_env_file "$ENV_FILE"

: "${POSTGRES_DB:=shield}"
: "${POSTGRES_USER:=shield}"
: "${POSTGRES_PASSWORD:=shield}"
: "${SPRING_PROFILES_ACTIVE:=prod}"
: "${JWT_SECRET:=change-me-change-me-change-me-change-me}"
: "${JWT_ACCESS_TOKEN_TTL_MINUTES:=30}"
: "${JWT_REFRESH_TOKEN_TTL_MINUTES:=4320}"
: "${SWAGGER_API_DOCS_ENABLED:=false}"
: "${SWAGGER_UI_ENABLED:=false}"
: "${PASSWORD_POLICY_MIN_LENGTH:=12}"
: "${PASSWORD_POLICY_MAX_LENGTH:=128}"
: "${PASSWORD_POLICY_REQUIRE_UPPER:=true}"
: "${PASSWORD_POLICY_REQUIRE_LOWER:=true}"
: "${PASSWORD_POLICY_REQUIRE_DIGIT:=true}"
: "${PASSWORD_POLICY_REQUIRE_SPECIAL:=true}"
: "${USER_LOCKOUT_MAX_FAILED_ATTEMPTS:=5}"
: "${USER_LOCKOUT_DURATION_MINUTES:=30}"
: "${NOTIFICATION_EMAIL_ENABLED:=false}"
: "${NOTIFICATION_EMAIL_FROM:=no-reply@shield.local}"
: "${SPRING_MAIL_HOST:=smtp.gmail.com}"
: "${SPRING_MAIL_PORT:=587}"
: "${SPRING_MAIL_USERNAME:=}"
: "${SPRING_MAIL_PASSWORD:=}"
: "${NOTIFICATION_SMS_ENABLED:=false}"
: "${NOTIFICATION_SMS_PROVIDER:=DUMMY}"
: "${NOTIFICATION_WHATSAPP_ENABLED:=false}"
: "${NOTIFICATION_WHATSAPP_PROVIDER:=DUMMY}"
: "${ROOT_EMAIL_VERIFICATION_PROVIDER:=DUMMY}"
: "${ROOT_MOBILE_VERIFICATION_PROVIDER:=DUMMY}"
: "${ROOT_BOOTSTRAP_CREDENTIAL_FILE:=./root-bootstrap-credential.txt}"
: "${ROOT_LOCKOUT_MAX_FAILED_ATTEMPTS:=5}"
: "${ROOT_LOCKOUT_DURATION_MINUTES:=30}"
: "${BOOTSTRAP_ENABLED:=false}"
: "${BOOTSTRAP_TENANT_NAME:=}"
: "${BOOTSTRAP_TENANT_ADDRESS:=}"
: "${BOOTSTRAP_ADMIN_NAME:=ShieldAdmin}"
: "${BOOTSTRAP_ADMIN_EMAIL:=}"
: "${BOOTSTRAP_ADMIN_PASSWORD:=}"
: "${PAYMENT_WEBHOOK_PROVIDER_SECRETS:=}"
: "${PAYMENT_WEBHOOK_REQUIRE_PROVIDER_SECRET:=true}"

export POSTGRES_DB POSTGRES_USER POSTGRES_PASSWORD SPRING_PROFILES_ACTIVE JWT_SECRET
export JWT_ACCESS_TOKEN_TTL_MINUTES JWT_REFRESH_TOKEN_TTL_MINUTES
export SWAGGER_API_DOCS_ENABLED SWAGGER_UI_ENABLED
export PASSWORD_POLICY_MIN_LENGTH PASSWORD_POLICY_MAX_LENGTH
export PASSWORD_POLICY_REQUIRE_UPPER PASSWORD_POLICY_REQUIRE_LOWER
export PASSWORD_POLICY_REQUIRE_DIGIT PASSWORD_POLICY_REQUIRE_SPECIAL
export USER_LOCKOUT_MAX_FAILED_ATTEMPTS USER_LOCKOUT_DURATION_MINUTES
export NOTIFICATION_EMAIL_ENABLED NOTIFICATION_EMAIL_FROM
export SPRING_MAIL_HOST SPRING_MAIL_PORT SPRING_MAIL_USERNAME SPRING_MAIL_PASSWORD
export NOTIFICATION_SMS_ENABLED NOTIFICATION_SMS_PROVIDER
export NOTIFICATION_WHATSAPP_ENABLED NOTIFICATION_WHATSAPP_PROVIDER
export ROOT_EMAIL_VERIFICATION_PROVIDER ROOT_MOBILE_VERIFICATION_PROVIDER
export ROOT_BOOTSTRAP_CREDENTIAL_FILE
export ROOT_LOCKOUT_MAX_FAILED_ATTEMPTS ROOT_LOCKOUT_DURATION_MINUTES
export BOOTSTRAP_ENABLED BOOTSTRAP_TENANT_NAME BOOTSTRAP_TENANT_ADDRESS
export BOOTSTRAP_ADMIN_NAME BOOTSTRAP_ADMIN_EMAIL BOOTSTRAP_ADMIN_PASSWORD
export PAYMENT_WEBHOOK_PROVIDER_SECRETS PAYMENT_WEBHOOK_REQUIRE_PROVIDER_SECRET

SYSTEM_DIR_NAME="System${INSTANCES}Nodes$(proxy_label "$PROXY")"
TARGET_DIR="$ROOT_DIR/system_topologies/generated/$SYSTEM_DIR_NAME"
COMPOSE_FILE="$TARGET_DIR/docker-compose.yml"
COMPOSE_PROJECT="$(echo "$SYSTEM_DIR_NAME" | tr '[:upper:]' '[:lower:]')"

mkdir -p "$POSTGRES_HOST_DIR/pgdata" "$REDIS_HOST_DIR" "$TARGET_DIR"

generate_haproxy_cfg() {
  local cfg_file="$TARGET_DIR/haproxy.cfg"
  {
    echo "global"
    echo "  log stdout format raw local0"
    echo
    echo "defaults"
    echo "  mode http"
    echo "  log global"
    echo "  option httplog"
    echo "  timeout connect 5s"
    echo "  timeout client 30s"
    echo "  timeout server 30s"
    echo
    echo "frontend fe_http"
    echo "  bind *:80"
    echo "  default_backend be_shield"
    echo
    echo "backend be_shield"
    echo "  balance roundrobin"
    for i in $(seq 1 "$INSTANCES"); do
      echo "  server app${i} app${i}:8080 check inter 5s fall 3 rise 2"
    done
  } > "$cfg_file"
}

generate_nginx_conf() {
  local cfg_file="$TARGET_DIR/nginx.conf"
  {
    echo "upstream shield_backend {"
    echo "  least_conn;"
    for i in $(seq 1 "$INSTANCES"); do
      echo "  server app${i}:8080 max_fails=3 fail_timeout=30s;"
    done
    echo "}"
    echo
    echo "server {"
    echo "  listen 80;"
    echo
    echo "  location / {"
    echo "    proxy_pass http://shield_backend;"
    echo "    proxy_http_version 1.1;"
    echo "    proxy_set_header Host \$host;"
    echo "    proxy_set_header X-Real-IP \$remote_addr;"
    echo "    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;"
    echo "    proxy_set_header X-Forwarded-Proto \$scheme;"
    echo "  }"
    echo "}"
  } > "$cfg_file"
}

generate_compose() {
  {
    echo "services:"
    echo "  postgres:"
    echo "    image: postgres:15"
    echo "    restart: unless-stopped"
    echo "    environment:"
    echo "      POSTGRES_DB: \${POSTGRES_DB}"
    echo "      POSTGRES_USER: \${POSTGRES_USER}"
    echo "      POSTGRES_PASSWORD: \${POSTGRES_PASSWORD}"
    echo "      PGDATA: /var/lib/postgresql/data/pgdata"
    echo "    volumes:"
    echo "      - $POSTGRES_HOST_DIR:/var/lib/postgresql/data"
    echo "    healthcheck:"
    echo "      test: [\"CMD-SHELL\", \"pg_isready -h 127.0.0.1 -U \${POSTGRES_USER} -d \${POSTGRES_DB} || exit 1\"]"
    echo "      interval: 10s"
    echo "      timeout: 5s"
    echo "      retries: 10"
    echo "    networks: [shield-net]"
    echo
    echo "  redis:"
    echo "    image: redis:7-alpine"
    echo "    restart: unless-stopped"
    echo "    command: [\"redis-server\", \"--appendonly\", \"yes\"]"
    echo "    volumes:"
    echo "      - $REDIS_HOST_DIR:/data"
    echo "    networks: [shield-net]"
    echo

    for i in $(seq 1 "$INSTANCES"); do
      echo "  app${i}:"
      echo "    build:"
      echo "      context: ../../.."
      echo "    restart: unless-stopped"
      echo "    environment:"
      echo "      SPRING_PROFILES_ACTIVE: \${SPRING_PROFILES_ACTIVE}"
      echo "      SERVER_PORT: 8080"
      echo "      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/\${POSTGRES_DB}"
      echo "      SPRING_DATASOURCE_USERNAME: \${POSTGRES_USER}"
      echo "      SPRING_DATASOURCE_PASSWORD: \${POSTGRES_PASSWORD}"
      echo "      JWT_SECRET: \${JWT_SECRET}"
      echo "      JWT_ACCESS_TOKEN_TTL_MINUTES: \${JWT_ACCESS_TOKEN_TTL_MINUTES}"
      echo "      JWT_REFRESH_TOKEN_TTL_MINUTES: \${JWT_REFRESH_TOKEN_TTL_MINUTES}"
      echo "      SWAGGER_API_DOCS_ENABLED: \${SWAGGER_API_DOCS_ENABLED}"
      echo "      SWAGGER_UI_ENABLED: \${SWAGGER_UI_ENABLED}"
      echo "      PASSWORD_POLICY_MIN_LENGTH: \${PASSWORD_POLICY_MIN_LENGTH}"
      echo "      PASSWORD_POLICY_MAX_LENGTH: \${PASSWORD_POLICY_MAX_LENGTH}"
      echo "      PASSWORD_POLICY_REQUIRE_UPPER: \${PASSWORD_POLICY_REQUIRE_UPPER}"
      echo "      PASSWORD_POLICY_REQUIRE_LOWER: \${PASSWORD_POLICY_REQUIRE_LOWER}"
      echo "      PASSWORD_POLICY_REQUIRE_DIGIT: \${PASSWORD_POLICY_REQUIRE_DIGIT}"
      echo "      PASSWORD_POLICY_REQUIRE_SPECIAL: \${PASSWORD_POLICY_REQUIRE_SPECIAL}"
      echo "      USER_LOCKOUT_MAX_FAILED_ATTEMPTS: \${USER_LOCKOUT_MAX_FAILED_ATTEMPTS}"
      echo "      USER_LOCKOUT_DURATION_MINUTES: \${USER_LOCKOUT_DURATION_MINUTES}"
      echo "      NOTIFICATION_EMAIL_ENABLED: \${NOTIFICATION_EMAIL_ENABLED}"
      echo "      NOTIFICATION_EMAIL_FROM: \${NOTIFICATION_EMAIL_FROM}"
      echo "      SPRING_MAIL_HOST: \${SPRING_MAIL_HOST}"
      echo "      SPRING_MAIL_PORT: \${SPRING_MAIL_PORT}"
      echo "      SPRING_MAIL_USERNAME: \${SPRING_MAIL_USERNAME}"
      echo "      SPRING_MAIL_PASSWORD: \${SPRING_MAIL_PASSWORD}"
      echo "      NOTIFICATION_SMS_ENABLED: \${NOTIFICATION_SMS_ENABLED}"
      echo "      NOTIFICATION_SMS_PROVIDER: \${NOTIFICATION_SMS_PROVIDER}"
      echo "      NOTIFICATION_WHATSAPP_ENABLED: \${NOTIFICATION_WHATSAPP_ENABLED}"
      echo "      NOTIFICATION_WHATSAPP_PROVIDER: \${NOTIFICATION_WHATSAPP_PROVIDER}"
      echo "      ROOT_EMAIL_VERIFICATION_PROVIDER: \${ROOT_EMAIL_VERIFICATION_PROVIDER}"
      echo "      ROOT_MOBILE_VERIFICATION_PROVIDER: \${ROOT_MOBILE_VERIFICATION_PROVIDER}"
      echo "      ROOT_BOOTSTRAP_CREDENTIAL_FILE: \${ROOT_BOOTSTRAP_CREDENTIAL_FILE}"
      echo "      ROOT_LOCKOUT_MAX_FAILED_ATTEMPTS: \${ROOT_LOCKOUT_MAX_FAILED_ATTEMPTS}"
      echo "      ROOT_LOCKOUT_DURATION_MINUTES: \${ROOT_LOCKOUT_DURATION_MINUTES}"
      echo "      BOOTSTRAP_ENABLED: \${BOOTSTRAP_ENABLED}"
      echo "      BOOTSTRAP_TENANT_NAME: \${BOOTSTRAP_TENANT_NAME}"
      echo "      BOOTSTRAP_TENANT_ADDRESS: \${BOOTSTRAP_TENANT_ADDRESS}"
      echo "      BOOTSTRAP_ADMIN_NAME: \${BOOTSTRAP_ADMIN_NAME}"
      echo "      BOOTSTRAP_ADMIN_EMAIL: \${BOOTSTRAP_ADMIN_EMAIL}"
      echo "      BOOTSTRAP_ADMIN_PASSWORD: \${BOOTSTRAP_ADMIN_PASSWORD}"
      echo "      PAYMENT_WEBHOOK_PROVIDER_SECRETS: \${PAYMENT_WEBHOOK_PROVIDER_SECRETS}"
      echo "      PAYMENT_WEBHOOK_REQUIRE_PROVIDER_SECRET: \${PAYMENT_WEBHOOK_REQUIRE_PROVIDER_SECRET}"
      echo "    depends_on:"
      echo "      postgres:"
      echo "        condition: service_healthy"
      echo "      redis:"
      echo "        condition: service_started"
      echo "    networks: [shield-net]"
      echo
    done

    if [[ "$PROXY" == "haproxy" ]]; then
      echo "  proxy:"
      echo "    image: haproxy:2.9"
      echo "    restart: unless-stopped"
      echo "    depends_on:"
      for i in $(seq 1 "$INSTANCES"); do
        echo "      - app${i}"
      done
      echo "    ports:"
      echo "      - \"8080:80\""
      echo "    volumes:"
      echo "      - ./haproxy.cfg:/usr/local/etc/haproxy/haproxy.cfg:ro"
      echo "    networks: [shield-net]"
    else
      echo "  proxy:"
      echo "    image: nginx:1.27-alpine"
      echo "    restart: unless-stopped"
      echo "    depends_on:"
      for i in $(seq 1 "$INSTANCES"); do
        echo "      - app${i}"
      done
      echo "    ports:"
      echo "      - \"8080:80\""
      echo "    volumes:"
      echo "      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro"
      echo "    networks: [shield-net]"
    fi

    echo
    echo "networks:"
    echo "  shield-net:"
    echo "    driver: bridge"
  } > "$COMPOSE_FILE"
}

if [[ "$PROXY" == "haproxy" ]]; then
  generate_haproxy_cfg
else
  generate_nginx_conf
fi

generate_compose

echo "Generated topology: $TARGET_DIR"
echo "Compose file: $COMPOSE_FILE"
echo "Using env file: $ENV_FILE"

if [[ "$GENERATE_ONLY" == true ]]; then
  echo "Generation only requested. Skipping docker compose command."
  exit 0
fi

COMPOSE_CMD=(docker compose --project-name "$COMPOSE_PROJECT" --env-file "$ENV_FILE" -f "$COMPOSE_FILE")

if [[ "$ACTION" == "down" ]]; then
  "${COMPOSE_CMD[@]}" down
  echo "Stopped topology $SYSTEM_DIR_NAME"
  exit 0
fi

if ! "${COMPOSE_CMD[@]}" up -d --build; then
  print_failure_diagnostics "$COMPOSE_PROJECT"
  exit 1
fi

echo "Started topology $SYSTEM_DIR_NAME"
echo "Proxy endpoint: http://localhost:8080"
