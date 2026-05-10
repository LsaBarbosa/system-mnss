#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
ONLINE_DIR="$REPO_ROOT/infra/online"
LOCAL_DIR="$REPO_ROOT/infra/local"

echo "==> Subindo stack online (simulação local)"
(
  cd "$ONLINE_DIR"
  MNSS_ONLINE_API_PORT="${MNSS_ONLINE_API_PORT:-8081}" \
  ONLINE_FRONT_PORT="${ONLINE_FRONT_PORT:-4201}" \
  ONLINE_FRONT_BUILD_CONFIGURATION="${ONLINE_FRONT_BUILD_CONFIGURATION:-online-local}" \
  MNSS_HSTS_ENABLED="${MNSS_HSTS_ENABLED:-false}" \
  docker compose up -d --build
)

echo "==> Subindo stack local"
(
  cd "$LOCAL_DIR"
  MNSS_ONLINE_URL="${MNSS_ONLINE_URL:-http://nova-alianca-online-api:8081}" \
  MNSS_SYNC_REQUIRE_HTTPS="${MNSS_SYNC_REQUIRE_HTTPS:-false}" \
  docker compose up -d --build
)

echo ""
echo "Stacks em execução:"
echo "- Local UI/API (nginx local): http://localhost"
echo "- Online API (direto):      http://localhost:8081/api/health"
echo "- Online front (direto):    http://localhost:4201"
echo "- Online edge opcional:     cd infra/online && docker compose --profile edge up -d"
