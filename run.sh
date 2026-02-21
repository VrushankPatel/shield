#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

INSTANCES=2
PROXY="haproxy"
ACTION="up"
GENERATE_ONLY=false
ENV_FILE="$ROOT_DIR/prod.env"

usage() {
  cat <<USAGE
Usage:
  ./run.sh --instances <count> --proxy <haproxy|nginx> [--env-file <path>] [--generate-only]
  ./run.sh --instances <count> --proxy <haproxy|nginx> [--env-file <path>] --down

Examples:
  ./run.sh --instances 4 --proxy haproxy --env-file prod.env
  ./run.sh --instances 2 --proxy nginx --env-file dev.env
  ./run.sh --instances 2 --proxy haproxy --generate-only

Options:
  --instances <count>   Number of app instances to generate/run (>=1)
  --proxy <name>        Proxy to use: haproxy or nginx
  --env-file <path>     Environment file to load (default: prod.env)
  --generate-only       Generate topology files only, do not run docker compose
  --down                Stop and remove generated topology stack
  -h, --help            Show this help
USAGE
}

require_value() {
  local flag="$1"
  local value="${2:-}"
  if [[ -z "$value" || "$value" == --* ]]; then
    echo "Missing value for $flag" >&2
    usage
    exit 1
  fi
}

resolve_path() {
  local input_path="$1"
  if [[ "$input_path" = /* ]]; then
    echo "$input_path"
  else
    echo "$ROOT_DIR/$input_path"
  fi
}

load_env_file() {
  local env_file="$1"
  if [[ ! -f "$env_file" ]]; then
    echo "Env file not found: $env_file" >&2
    exit 1
  fi

  set -a
  # shellcheck disable=SC1090
  source "$env_file"
  set +a
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
      ENV_FILE="$(resolve_path "$2")"
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
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if ! [[ "$INSTANCES" =~ ^[0-9]+$ ]]; then
  echo "--instances must be a positive integer" >&2
  exit 1
fi

if (( INSTANCES < 1 )); then
  echo "--instances must be >= 1" >&2
  exit 1
fi

case "$PROXY" in
  haproxy|nginx) ;;
  *)
    echo "--proxy must be either 'haproxy' or 'nginx'" >&2
    exit 1
    ;;
esac

load_env_file "$ENV_FILE"

: "${POSTGRES_DB:=shield}"
: "${POSTGRES_USER:=shield}"
: "${POSTGRES_PASSWORD:=shield}"
: "${SPRING_PROFILES_ACTIVE:=prod}"
: "${JWT_SECRET:=change-me-change-me-change-me-change-me}"
: "${JWT_ACCESS_TOKEN_TTL_MINUTES:=30}"
: "${JWT_REFRESH_TOKEN_TTL_MINUTES:=4320}"
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
export NOTIFICATION_EMAIL_ENABLED NOTIFICATION_EMAIL_FROM
export SPRING_MAIL_HOST SPRING_MAIL_PORT SPRING_MAIL_USERNAME SPRING_MAIL_PASSWORD
export NOTIFICATION_SMS_ENABLED NOTIFICATION_SMS_PROVIDER
export NOTIFICATION_WHATSAPP_ENABLED NOTIFICATION_WHATSAPP_PROVIDER
export ROOT_EMAIL_VERIFICATION_PROVIDER ROOT_MOBILE_VERIFICATION_PROVIDER
export ROOT_BOOTSTRAP_CREDENTIAL_FILE
export ROOT_LOCKOUT_MAX_FAILED_ATTEMPTS ROOT_LOCKOUT_DURATION_MINUTES
export BOOTSTRAP_ENABLED BOOTSTRAP_TENANT_NAME BOOTSTRAP_TENANT_ADDRESS
export BOOTSTRAP_ADMIN_NAME BOOTSTRAP_ADMIN_EMAIL BOOTSTRAP_ADMIN_PASSWORD
export PAYMENT_WEBHOOK_PROVIDER_SECRETS
export PAYMENT_WEBHOOK_REQUIRE_PROVIDER_SECRET

proxy_label() {
  if [[ "$1" == "haproxy" ]]; then
    echo "HaProxy"
  else
    echo "Nginx"
  fi
}

SYSTEM_DIR_NAME="System${INSTANCES}Nodes$(proxy_label "$PROXY")"
TARGET_DIR="$ROOT_DIR/system_topologies/generated/$SYSTEM_DIR_NAME"
COMPOSE_FILE="$TARGET_DIR/docker-compose.yml"
COMPOSE_PROJECT="$(echo "$SYSTEM_DIR_NAME" | tr '[:upper:]' '[:lower:]')"

mkdir -p "$ROOT_DIR/db_files/postgres" "$ROOT_DIR/db_files/redis"
mkdir -p "$TARGET_DIR"

generate_haproxy_cfg() {
  local cfg_file="$TARGET_DIR/haproxy.cfg"
  {
    echo "global"
    echo "  log stdout format raw local0"
    echo ""
    echo "defaults"
    echo "  mode http"
    echo "  log global"
    echo "  option httplog"
    echo "  timeout connect 5s"
    echo "  timeout client 30s"
    echo "  timeout server 30s"
    echo ""
    echo "frontend fe_http"
    echo "  bind *:80"
    echo "  default_backend be_shield"
    echo ""
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
    echo ""
    echo "server {"
    echo "  listen 80;"
    echo ""
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
    echo "    volumes:"
    echo "      - ../../../db_files/postgres:/var/lib/postgresql/data"
    echo "    healthcheck:"
    echo "      test: [\"CMD-SHELL\", \"pg_isready -U \${POSTGRES_USER}\"]"
    echo "      interval: 10s"
    echo "      timeout: 5s"
    echo "      retries: 5"
    echo "    networks: [shield-net]"
    echo ""
    echo "  redis:"
    echo "    image: redis:7-alpine"
    echo "    restart: unless-stopped"
    echo "    command: [\"redis-server\", \"--appendonly\", \"yes\"]"
    echo "    volumes:"
    echo "      - ../../../db_files/redis:/data"
    echo "    networks: [shield-net]"
    echo ""

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
      echo ""
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

    echo ""
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

if [[ "$ACTION" == "down" ]]; then
  docker compose --project-name "$COMPOSE_PROJECT" --env-file "$ENV_FILE" -f "$COMPOSE_FILE" down
  echo "Stopped topology $SYSTEM_DIR_NAME"
  exit 0
fi

docker compose --project-name "$COMPOSE_PROJECT" --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --build

echo "Started topology $SYSTEM_DIR_NAME"
echo "Proxy endpoint: http://localhost:8080"
