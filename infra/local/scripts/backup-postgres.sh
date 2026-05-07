#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_DIR="${BACKUP_DIR:-$SCRIPT_DIR/../postgres/backup}"
DATE=$(date +"%Y-%m-%d_%H-%M-%S")
CONTAINER="postgres-local"
DB="nova_alianca_local"
USER="nova_alianca"

mkdir -p "$BACKUP_DIR"

docker exec $CONTAINER pg_dump -U $USER $DB > "$BACKUP_DIR/backup_$DATE.sql"

# Manter apenas 15 dias
find "$BACKUP_DIR" -type f -name "*.sql" -mtime +15 -delete
