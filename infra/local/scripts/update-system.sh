#!/bin/bash

# Navegar para o diretório do projeto (ajuste se necessário)
cd "$(dirname "$0")/.."

docker compose pull
docker compose up -d
docker image prune -f
