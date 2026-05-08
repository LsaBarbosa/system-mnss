#!/usr/bin/env bash
# Verifica saúde dos contêineres Docker e conectividade de infra (sem depender da API).
set -euo pipefail

COMPOSE_DIR="${COMPOSE_DIR:-$(cd "$(dirname "$0")/.." && pwd)}"

pass() { echo "[OK]  $*"; }
fail() { echo "[FAIL] $*"; FAILURES=$((FAILURES + 1)); }

FAILURES=0

echo "=== Smoke infra local — $(date '+%Y-%m-%d %H:%M:%S') ==="
echo "Compose dir: $COMPOSE_DIR"
echo ""

# ── 1. Contêineres rodando e healthy ──────────────────────────────────────────
echo "--- Contêineres Docker ---"
for svc in postgres-local rabbitmq-local redis-local nova-alianca-local-api nginx-local; do
  status=$(docker inspect --format '{{.State.Health.Status}}' "$svc" 2>/dev/null || echo "absent")
  if [[ "$status" == "healthy" ]]; then
    pass "$svc: healthy"
  elif [[ "$status" == "absent" ]]; then
    fail "$svc: contêiner não encontrado"
  else
    fail "$svc: $status"
  fi
done

echo ""

# ── 2. PostgreSQL: conectividade e banco existente ────────────────────────────
echo "--- PostgreSQL ---"
if docker exec postgres-local pg_isready -U nova_alianca -d nova_alianca_local -q 2>/dev/null; then
  pass "postgres-local: pronto para conexões"
else
  fail "postgres-local: pg_isready falhou"
fi

TABLE_COUNT=$(docker exec postgres-local psql -U nova_alianca -d nova_alianca_local -tAc \
  "SELECT count(*) FROM information_schema.tables WHERE table_schema='public'" 2>/dev/null || echo "0")
if [[ "${TABLE_COUNT:-0}" -ge 10 ]]; then
  pass "postgres-local: $TABLE_COUNT tabelas no schema public"
else
  fail "postgres-local: apenas $TABLE_COUNT tabelas — migrations podem não ter rodado"
fi

echo ""

# ── 3. RabbitMQ: management API ───────────────────────────────────────────────
echo "--- RabbitMQ ---"
if curl -fsS --max-time 5 -u "nova_alianca:${RABBITMQ_DEFAULT_PASS:-nova_alianca}" \
     http://localhost:15672/api/healthchecks/node >/dev/null 2>&1; then
  pass "rabbitmq-local: management API respondeu"
else
  fail "rabbitmq-local: management API não respondeu (verifique RABBITMQ_DEFAULT_PASS)"
fi

echo ""

# ── 4. Redis: PING ────────────────────────────────────────────────────────────
echo "--- Redis ---"
PONG=$(docker exec redis-local redis-cli ping 2>/dev/null || echo "")
if [[ "$PONG" == "PONG" ]]; then
  pass "redis-local: PING -> PONG"
else
  fail "redis-local: PING falhou"
fi

echo ""

# ── 5. Resultado ──────────────────────────────────────────────────────────────
if [[ "$FAILURES" -eq 0 ]]; then
  echo "=== Todos os checks de infra passaram. ==="
  exit 0
else
  echo "=== $FAILURES check(s) falharam. Verifique os contêineres acima. ==="
  exit 1
fi
