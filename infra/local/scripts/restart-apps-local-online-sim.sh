#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
ONLINE_DIR="$REPO_ROOT/infra/online"
LOCAL_DIR="$REPO_ROOT/infra/local"

REBUILD="${1:-}"

echo "==> Reiniciando aplicações sem derrubar bancos, Redis e RabbitMQ"
echo ""

if [[ "$REBUILD" == "--rebuild" ]]; then
  echo "==> Rebuild das aplicações online"
  (
    cd "$ONLINE_DIR"

    MNSS_ONLINE_API_PORT="${MNSS_ONLINE_API_PORT:-8081}" \
    ONLINE_FRONT_PORT="${ONLINE_FRONT_PORT:-4201}" \
    ONLINE_FRONT_BUILD_CONFIGURATION="${ONLINE_FRONT_BUILD_CONFIGURATION:-online-local}" \
    MNSS_HSTS_ENABLED="${MNSS_HSTS_ENABLED:-false}" \
    docker compose up -d --build --no-deps nova-alianca-online-api nova-alianca-frontend

    docker compose restart nginx || true
  )

  echo ""
  echo "==> Rebuild das aplicações locais"
  (
    cd "$LOCAL_DIR"

    MNSS_ONLINE_URL="${MNSS_ONLINE_URL:-http://nova-alianca-online-api:8081}" \
    MNSS_SYNC_REQUIRE_HTTPS="${MNSS_SYNC_REQUIRE_HTTPS:-false}" \
    docker compose up -d --build --no-deps nova-alianca-local-api nova-alianca-local-front

    docker compose restart nginx-local || true
  )
else
  echo "==> Restart das aplicações online"
  (
    cd "$ONLINE_DIR"
    docker compose restart nova-alianca-online-api nova-alianca-frontend nginx
  )

  echo ""
  echo "==> Restart das aplicações locais"
  (
    cd "$LOCAL_DIR"
    docker compose restart nova-alianca-local-api nova-alianca-local-front nginx-local
  )
fi

echo ""
echo "==> Status dos containers principais"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "nova-alianca|nginx|postgres|rabbitmq|redis" || true

echo ""
echo "Aplicações reiniciadas."
echo "- Local UI/API:        http://localhost"
echo "- Online API direto:   http://localhost:8081/api/health"
echo "- Online front direto: http://localhost:4201"
