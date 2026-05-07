#!/bin/bash
# certbot-init.sh — Executar UMA VEZ antes do primeiro deploy
#
# Problema: nginx precisa de certificados SSL para iniciar, mas o certbot
# precisa do nginx rodando para completar o desafio HTTP-01.
#
# Solução: gera certificados auto-assinados temporários → sobe nginx →
# solicita certificados reais do Let's Encrypt → recarrega nginx.
#
# Uso:
#   export CERTBOT_EMAIL=admin@padarianovaalianca.com.br
#   bash infra/online/scripts/certbot-init.sh

set -euo pipefail

COMPOSE_FILE="$(dirname "$0")/../docker-compose.yml"
EMAIL="${CERTBOT_EMAIL:-admin@padarianovaalianca.com.br}"
CERTBOT_DIR="$(dirname "$0")/../certbot/conf"
WEBROOT_DIR="$(dirname "$0")/../certbot/www"

DOMAINS=(
    "api.padarianovaalianca.com.br"
    "padarianovaalianca.com.br"
    "admin.padarianovaalianca.com.br"
)

mkdir -p "$WEBROOT_DIR"

# ── Passo 1: certificados auto-assinados temporários ─────────────────────────
echo "[1/4] Gerando certificados temporários para nginx iniciar..."
for DOMAIN in "${DOMAINS[@]}"; do
    CERT_PATH="$CERTBOT_DIR/live/$DOMAIN"
    if [ ! -f "$CERT_PATH/fullchain.pem" ]; then
        mkdir -p "$CERT_PATH"
        openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
            -keyout "$CERT_PATH/privkey.pem" \
            -out    "$CERT_PATH/fullchain.pem" \
            -subj   "/CN=$DOMAIN" 2>/dev/null
        echo "   → certificado temporário criado para $DOMAIN"
    else
        echo "   → certificado já existe para $DOMAIN, pulando"
    fi
done

# ── Passo 2: subir nginx com certificados temporários ────────────────────────
echo "[2/4] Subindo nginx..."
docker compose -f "$COMPOSE_FILE" up -d nginx
sleep 5

# ── Passo 3: solicitar certificados reais via Let's Encrypt ──────────────────
echo "[3/4] Solicitando certificados Let's Encrypt para: ${DOMAINS[*]}"

DOMAIN_ARGS=""
for DOMAIN in "${DOMAINS[@]}"; do
    DOMAIN_ARGS="$DOMAIN_ARGS -d $DOMAIN"
done

docker compose -f "$COMPOSE_FILE" run --rm certbot \
    certonly --webroot \
    --webroot-path /var/www/certbot \
    $DOMAIN_ARGS \
    --email "$EMAIL" \
    --agree-tos \
    --no-eff-email \
    --force-renewal

# ── Passo 4: recarregar nginx com certificados reais ─────────────────────────
echo "[4/4] Recarregando nginx com certificados reais..."
docker compose -f "$COMPOSE_FILE" exec nginx nginx -s reload

echo ""
echo "✓ Certificados Let's Encrypt emitidos com sucesso."
echo "  Renovação automática: o container 'certbot' renova a cada 12h automaticamente."
