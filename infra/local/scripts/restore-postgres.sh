#!/bin/bash
set -euo pipefail

BACKUP_DIR="/backup"
CONTAINER="postgres-local"
DB="nova_alianca_local"
USER="nova_alianca"

if [ -z "${1:-}" ]; then
    echo "Uso: $0 <arquivo_backup.sql>"
    echo ""
    echo "Backups disponíveis em $BACKUP_DIR:"
    ls -lt "$BACKUP_DIR"/*.sql 2>/dev/null || echo "  Nenhum backup encontrado."
    exit 1
fi

BACKUP_FILE="$1"

if [ ! -f "$BACKUP_FILE" ]; then
    echo "Erro: arquivo '$BACKUP_FILE' não encontrado."
    exit 1
fi

echo "Restaurando '$BACKUP_FILE' no banco '$DB' (container: $CONTAINER)..."

# Encerra conexões ativas para permitir drop/recreate
docker exec "$CONTAINER" psql -U "$USER" -c \
    "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='$DB' AND pid <> pg_backend_pid();" postgres

docker exec "$CONTAINER" psql -U "$USER" -c "DROP DATABASE IF EXISTS $DB;" postgres
docker exec "$CONTAINER" psql -U "$USER" -c "CREATE DATABASE $DB;" postgres

docker exec -i "$CONTAINER" psql -U "$USER" "$DB" < "$BACKUP_FILE"

echo "Restauração concluída."
