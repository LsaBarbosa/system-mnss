#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${ENV_FILE:-${SCRIPT_DIR}/../.env}"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  . "${ENV_FILE}"
  set +a
fi

BACKUP_DIR="${BACKUP_DIR:-${SCRIPT_DIR}/../postgres/backup}"
CONTAINER="${POSTGRES_CONTAINER:-postgres-online}"
DB="${POSTGRES_DB:-nova_alianca_online}"
USER="${POSTGRES_USER:-nova_alianca}"
RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-15}"
DATE="$(date +"%Y-%m-%d_%H-%M-%S")"
TARGET="${BACKUP_DIR}/${DB}_${DATE}.dump"

mkdir -p "${BACKUP_DIR}"

docker exec "${CONTAINER}" pg_dump -U "${USER}" --format=custom --no-owner --no-privileges "${DB}" > "${TARGET}"
find "${BACKUP_DIR}" -type f -name "${DB}_*.dump" -mtime +"${RETENTION_DAYS}" -delete

printf 'Backup online criado: %s\n' "${TARGET}"
