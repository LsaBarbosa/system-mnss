#!/usr/bin/env bash
# Restaura o banco online a partir de um dump SQL.
# ATENÇÃO: destrói o banco atual — cria backup automático antes.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_DIR="${BACKUP_DIR:-$SCRIPT_DIR/../postgres/backup}"
CONTAINER="postgres-online"
DB="${POSTGRES_DB:-nova_alianca_online}"
USER="${POSTGRES_USER:-nova_alianca}"

# ── 1. Argumento obrigatório ──────────────────────────────────────────────────
if [ -z "${1:-}" ]; then
    echo "Uso: $0 <arquivo_backup.sql>"
    echo ""
    echo "Backups disponíveis em $BACKUP_DIR:"
    ls -lt "$BACKUP_DIR"/*.sql 2>/dev/null || echo "  Nenhum backup encontrado."
    exit 1
fi

BACKUP_FILE="$1"

# ── 2. Validar arquivo ────────────────────────────────────────────────────────
if [ ! -f "$BACKUP_FILE" ]; then
    echo "Erro: arquivo '$BACKUP_FILE' não encontrado."
    exit 1
fi

# ── 3. Validar container ──────────────────────────────────────────────────────
if ! docker inspect --format '{{.State.Running}}' "$CONTAINER" 2>/dev/null | grep -q true; then
    echo "Erro: container '$CONTAINER' não está rodando."
    echo "Execute 'docker compose up -d' antes de restaurar."
    exit 1
fi

# ── 4. Confirmação explícita ──────────────────────────────────────────────────
echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║  ATENÇÃO: esta operação DESTRÓI o banco '$DB'    ║"
echo "║  Será criado um backup pré-restore antes do DROP.           ║"
echo "║  Este é o banco de PRODUÇÃO ONLINE. Prossiga com cuidado.  ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""
printf "Digite exatamente  RESTAURAR-%s  para confirmar: " "$DB"
read -r CONFIRM

if [ "$CONFIRM" != "RESTAURAR-$DB" ]; then
    echo "Confirmação incorreta. Operação cancelada."
    exit 1
fi

# ── 5. Backup pré-restore ─────────────────────────────────────────────────────
mkdir -p "$BACKUP_DIR"
PRE_RESTORE_FILE="$BACKUP_DIR/pre_restore_$(date +%Y-%m-%d_%H-%M-%S).sql"
echo ""
echo "Criando backup pré-restore em '$PRE_RESTORE_FILE'..."
if ! docker exec "$CONTAINER" pg_dump -U "$USER" "$DB" > "$PRE_RESTORE_FILE"; then
    echo "Erro ao criar backup pré-restore. Operação cancelada por segurança."
    rm -f "$PRE_RESTORE_FILE"
    exit 1
fi
echo "Backup pré-restore criado com sucesso."

# ── 6. Encerrar conexões, dropar e recriar banco ──────────────────────────────
echo "Encerrando conexões ativas ao banco '$DB'..."
docker exec "$CONTAINER" psql -U "$USER" -c \
    "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='$DB' AND pid <> pg_backend_pid();" postgres

docker exec "$CONTAINER" psql -U "$USER" -c "DROP DATABASE IF EXISTS $DB;" postgres
docker exec "$CONTAINER" psql -U "$USER" -c "CREATE DATABASE $DB;" postgres

# ── 7. Restaurar dump ─────────────────────────────────────────────────────────
echo "Restaurando '$BACKUP_FILE' no banco '$DB'..."
docker exec -i "$CONTAINER" psql -U "$USER" "$DB" < "$BACKUP_FILE"

echo ""
echo "Restauração concluída com sucesso."
echo "Backup pré-restore disponível em: $PRE_RESTORE_FILE"
