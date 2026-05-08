#!/usr/bin/env bash
# Smoke test funcional do PDV local.
# Autentica, abre caixa (se necessário), cria uma venda, paga e finaliza.
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost}"
USERNAME="${MNSS_SMOKE_USERNAME:-admin}"
PASSWORD="${MNSS_SMOKE_PASSWORD:-change_me}"

COOKIE_FILE=$(mktemp)
trap 'rm -f "$COOKIE_FILE"' EXIT

log()  { echo "[PDV-SMOKE] $*"; }
fail() { echo "[PDV-SMOKE] FAIL: $*" >&2; exit 1; }

# 1. Login
log "1/8 — Login como '$USERNAME'..."
LOGIN_STATUS=$(curl -fsS -o /dev/null -w "%{http_code}" \
  -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}" \
  -c "$COOKIE_FILE")
[ "$LOGIN_STATUS" = "200" ] || fail "Login retornou HTTP $LOGIN_STATUS"
log "   Login OK."

# 2. Listar produtos disponíveis no PDV
log "2/8 — Listando produtos PDV..."
PRODUCTS=$(curl -fsS "$BASE_URL/api/pdv/products" -b "$COOKIE_FILE" 2>/dev/null || echo "[]")
PRODUCT_ID=$(echo "$PRODUCTS" | python3 -c "
import sys, json
prods = json.load(sys.stdin)
if not prods: sys.exit(1)
print(prods[0]['id'])
" 2>/dev/null) || fail "Nenhum produto disponível no PDV."
PRODUCT_PRICE=$(echo "$PRODUCTS" | python3 -c "
import sys, json
prods = json.load(sys.stdin)
print(prods[0]['price'])
" 2>/dev/null)
log "   Produto selecionado: $PRODUCT_ID (preço: $PRODUCT_PRICE)"

# 3. Abrir caixa (tenta abrir; ignora 409 Conflict se já aberto)
log "3/8 — Abrindo caixa..."
CASH_STATUS=$(curl -fsS -o /dev/null -w "%{http_code}" \
  -X POST "$BASE_URL/api/cash-registers/open" \
  -H "Content-Type: application/json" \
  -d '{"initialAmount":"0.00"}' \
  -b "$COOKIE_FILE")
if [ "$CASH_STATUS" = "200" ] || [ "$CASH_STATUS" = "201" ] || [ "$CASH_STATUS" = "409" ]; then
  log "   Caixa OK (HTTP $CASH_STATUS)."
else
  fail "Abertura de caixa retornou HTTP $CASH_STATUS"
fi

# 4. Criar venda
log "4/8 — Criando venda..."
SALE=$(curl -fsS -X POST "$BASE_URL/api/pdv/sales" \
  -H "Content-Type: application/json" \
  -d '{"deliveryType":"LOCAL_CONSUMPTION"}' \
  -b "$COOKIE_FILE")
SALE_ID=$(echo "$SALE" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null) \
  || fail "Não foi possível obter o ID da venda."
log "   Venda criada: $SALE_ID"

# 5. Adicionar item
log "5/8 — Adicionando item à venda..."
ITEM_STATUS=$(curl -fsS -o /dev/null -w "%{http_code}" \
  -X POST "$BASE_URL/api/pdv/sales/$SALE_ID/items" \
  -H "Content-Type: application/json" \
  -d "{\"productId\":\"$PRODUCT_ID\",\"quantity\":1}" \
  -b "$COOKIE_FILE")
[ "$ITEM_STATUS" = "200" ] || [ "$ITEM_STATUS" = "201" ] \
  || fail "Adição de item retornou HTTP $ITEM_STATUS"
log "   Item adicionado."

# 6. Registrar pagamento
log "6/8 — Registrando pagamento..."
PAY_STATUS=$(curl -fsS -o /dev/null -w "%{http_code}" \
  -X POST "$BASE_URL/api/pdv/sales/$SALE_ID/payment" \
  -H "Content-Type: application/json" \
  -d "{\"method\":\"CASH\",\"amount\":$PRODUCT_PRICE}" \
  -b "$COOKIE_FILE")
[ "$PAY_STATUS" = "200" ] || [ "$PAY_STATUS" = "201" ] \
  || fail "Pagamento retornou HTTP $PAY_STATUS"
log "   Pagamento registrado."

# 7. Finalizar venda
log "7/8 — Finalizando venda (POST /finish)..."
FINISH_STATUS=$(curl -fsS -o /dev/null -w "%{http_code}" \
  -X POST "$BASE_URL/api/pdv/sales/$SALE_ID/finish" \
  -H "Content-Type: application/json" \
  -b "$COOKIE_FILE")
[ "$FINISH_STATUS" = "200" ] || [ "$FINISH_STATUS" = "204" ] \
  || fail "Finalização da venda retornou HTTP $FINISH_STATUS (esperado 200 ou 204)"
log "   Venda finalizada (HTTP $FINISH_STATUS)."

# 8. Verificar status final da venda
log "8/8 — Verificando status final da venda..."
SALE_DETAIL=$(curl -fsS "$BASE_URL/api/pdv/sales/$SALE_ID" -b "$COOKIE_FILE" 2>/dev/null || echo "{}")
SALE_STATUS=$(echo "$SALE_DETAIL" | python3 -c "
import sys,json; d=json.load(sys.stdin); print(d.get('status',''))
" 2>/dev/null)
log "   Status final: $SALE_STATUS"
[ -n "$SALE_STATUS" ] || fail "Não foi possível verificar o status da venda."
case "$SALE_STATUS" in
  PAID|SENT_TO_STORE|FINISHED) ;;
  *) fail "Status inesperado: '$SALE_STATUS' (esperado PAID, SENT_TO_STORE ou FINISHED)" ;;
esac

echo ""
echo "[PDV-SMOKE] ✓ Smoke test do PDV local concluído com sucesso. Status final: $SALE_STATUS"
