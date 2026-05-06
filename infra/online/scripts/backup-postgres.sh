#!/bin/bash

BACKUP_DIR="/backup"
DATE=$(date +"%Y-%m-%d_%H-%M-%S")
CONTAINER="postgres-online"
DB="nova_alianca_online"
USER="nova_alianca"

mkdir -p $BACKUP_DIR

docker exec $CONTAINER pg_dump -U $USER $DB > "$BACKUP_DIR/backup_$DATE.sql"

# Manter apenas 15 dias
find $BACKUP_DIR -type f -name "*.sql" -mtime +15 -delete
