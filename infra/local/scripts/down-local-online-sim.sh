#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
ONLINE_DIR="$REPO_ROOT/infra/online"
LOCAL_DIR="$REPO_ROOT/infra/local"

echo "==> Derrubando stack local"
(cd "$LOCAL_DIR" && docker compose down)

echo "==> Derrubando stack online (incluindo profile edge)"
(cd "$ONLINE_DIR" && docker compose --profile edge down)
