#!/bin/bash

# Navegar para o diretório do projeto (ajuste se necessário)
cd "$(dirname "$0")/.."

docker compose --profile edge pull
docker compose --profile edge up -d
docker image prune -f
