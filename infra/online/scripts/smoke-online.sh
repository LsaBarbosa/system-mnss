#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-https://api.padarianovaalianca.com.br}"

check() {
  local url="$BASE_URL$1"
  printf "Checking %s ... " "$url"
  if curl -fsS --max-time 10 "$url" >/dev/null; then
    echo "OK"
  else
    echo "FAIL"
    exit 1
  fi
}

echo "=== Smoke test online — ${BASE_URL} ==="

check "/api/health"
check "/api/version"
check "/api/public/info"
check "/api/public/menu"
check "/actuator/health"

echo ""
echo "=== All online smoke tests passed. ==="
