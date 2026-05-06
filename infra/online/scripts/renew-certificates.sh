#!/bin/bash
set -euo pipefail

COMPOSE_FILE="/opt/nova-alianca-online/docker-compose.yml"
CERTBOT_CONTAINER="certbot"
NGINX_CONTAINER="nginx"

echo "Verificando/renovando certificados Let's Encrypt..."

docker compose -f "$COMPOSE_FILE" run --rm "$CERTBOT_CONTAINER" renew

echo "Recarregando configuração do Nginx..."
docker compose -f "$COMPOSE_FILE" exec "$NGINX_CONTAINER" nginx -s reload

echo "Renovação concluída."
