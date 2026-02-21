#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

INSTANCES=2
PROXY="haproxy"
ACTION="up"
GENERATE_ONLY=false

usage() {
  cat <<USAGE
Usage:
  ./run.sh --instances <count> --proxy <haproxy|nginx> [--generate-only]
  ./run.sh --instances <count> --proxy <haproxy|nginx> --down

Examples:
  ./run.sh --instances 4 --proxy haproxy
  ./run.sh --instances 2 --proxy nginx
  ./run.sh --instances 2 --proxy haproxy --generate-only

Options:
  --instances <count>   Number of app instances to generate/run (>=1)
  --proxy <name>        Proxy to use: haproxy or nginx
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
    echo "      POSTGRES_DB: shield"
    echo "      POSTGRES_USER: shield"
    echo "      POSTGRES_PASSWORD: shield"
    echo "    volumes:"
    echo "      - ../../../db_files/postgres:/var/lib/postgresql/data"
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
      echo "      SPRING_PROFILES_ACTIVE: prod"
      echo "      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/shield"
      echo "      SPRING_DATASOURCE_USERNAME: shield"
      echo "      SPRING_DATASOURCE_PASSWORD: shield"
      echo "      JWT_SECRET: change-me-change-me-change-me-change-me"
      echo "    depends_on:"
      echo "      - postgres"
      echo "      - redis"
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

if [[ "$GENERATE_ONLY" == true ]]; then
  echo "Generation only requested. Skipping docker compose command."
  exit 0
fi

if [[ "$ACTION" == "down" ]]; then
  docker compose -f "$COMPOSE_FILE" down
  echo "Stopped topology $SYSTEM_DIR_NAME"
  exit 0
fi

docker compose -f "$COMPOSE_FILE" up -d --build

echo "Started topology $SYSTEM_DIR_NAME"
echo "Proxy endpoint: http://localhost:8080"
