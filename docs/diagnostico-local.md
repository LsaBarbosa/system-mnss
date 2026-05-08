# Diagnóstico Rápido — Ambiente Local

Comandos objetivos para identificar e resolver falhas no ambiente local da padaria Nova Aliança.

---

## 1. Estado geral dos serviços

```bash
cd infra/local
docker compose ps
```

Todos os serviços devem aparecer como `Up` e os healthchecks como `(healthy)`.

Se algum serviço estiver `Exit` ou `Restarting`:

```bash
docker compose up -d --no-recreate   # tenta subir os que caíram
docker compose ps                    # verifica novamente
```

---

## 2. Logs dos serviços

### API local

```bash
docker logs nova-alianca-local-api --tail=100
docker logs nova-alianca-local-api --tail=100 -f   # modo streaming
```

### Banco de dados

```bash
docker logs postgres-local --tail=50
```

### RabbitMQ

```bash
docker logs rabbitmq-local --tail=50
```

### Redis

```bash
docker logs redis-local --tail=50
```

### Nginx

```bash
docker logs nginx-local --tail=30
```

---

## 3. Health check da API

```bash
curl -s http://localhost/api/health | python3 -m json.tool
```

Resultado esperado: `"status": "UP"` com detalhes dos componentes (db, redis, rabbitmq).

```bash
# Verifica o actuator do Spring Boot diretamente (sem nginx)
curl -s http://localhost:8080/actuator/health | python3 -m json.tool
```

---

## 4. Verificar portas em uso

```bash
# Porta do nginx (default: 80)
ss -tlnp | grep :80

# Porta da API (default: 8080, exposta apenas para localhost)
ss -tlnp | grep :8080

# Porta do banco (default: 5432)
ss -tlnp | grep :5432
```

Se a porta estiver ocupada por outro processo:

```bash
# Identifica o processo que usa a porta
sudo lsof -i :80
sudo lsof -i :5432
```

---

## 5. Verificar volumes

```bash
# Lista todos os volumes do compose local
docker volume ls | grep local

# Inspeciona o volume do banco
docker volume inspect infra_local_postgres_local_data 2>/dev/null \
  || docker volume inspect system-mnss_postgres_local_data 2>/dev/null
```

---

## 6. Verificar uso de disco

```bash
# Espaço total dos volumes Docker
docker system df

# Espaço do volume específico do banco
docker exec postgres-local df -h /var/lib/postgresql/data
```

Se o disco estiver cheio, a API falha ao escrever no banco e o banco pode corromper WAL:

```bash
# Remove imagens e layers sem uso (seguro)
docker image prune -f
docker builder prune -f
```

---

## 7. Acessar o banco diretamente

```bash
# Conectar ao banco local
docker exec -it postgres-local psql -U nova_alianca -d nova_alianca_local

# Verificar migrations aplicadas
docker exec postgres-local psql -U nova_alianca -d nova_alianca_local \
  -c "SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank;"

# Verificar eventos de sync pendentes
docker exec postgres-local psql -U nova_alianca -d nova_alianca_local \
  -c "SELECT status, count(*) FROM sync_events GROUP BY status ORDER BY count DESC;"

# Verificar vendas recentes
docker exec postgres-local psql -U nova_alianca -d nova_alianca_local \
  -c "SELECT id, status, payment_status, created_at FROM orders ORDER BY created_at DESC LIMIT 10;"
```

---

## 8. Acessar o RabbitMQ

```bash
# Painel de administração (necessário expor a porta 15672 no compose)
# Se configurado: http://localhost:15672
# Usuário/senha: conforme RABBITMQ_DEFAULT_USER / RABBITMQ_DEFAULT_PASS no .env

# Verificar filas via CLI
docker exec rabbitmq-local rabbitmqctl list_queues name messages consumers
```

---

## 9. Acessar o Redis

```bash
# Verificar conexão
docker exec redis-local redis-cli ping   # deve retornar PONG

# Listar chaves (apenas em ambiente de desenvolvimento)
docker exec redis-local redis-cli keys '*'
```

---

## 10. Backup antes de mexer em volumes

Sempre faça backup antes de qualquer operação destrutiva em volumes:

```bash
# Backup do banco
docker exec postgres-local pg_dump -U nova_alianca nova_alianca_local \
  > backup_$(date +%Y%m%d_%H%M%S).sql

# Verificar se o backup foi gerado
ls -lh backup_*.sql | tail -5
```

O compose já monta `/backup` no container do banco para facilitar:

```bash
docker exec postgres-local pg_dump -U nova_alianca nova_alianca_local \
  -F c -f /backup/backup_$(date +%Y%m%d_%H%M%S).dump
```

---

## 11. Reiniciar serviços individualmente

```bash
# Reiniciar apenas a API (sem perder banco/fila)
docker compose restart nova-alianca-local-api

# Reiniciar tudo (preserva volumes)
docker compose down && docker compose up -d
```

---

## 12. Problemas comuns

| Sintoma | Causa provável | Ação |
|--------|---------------|------|
| API não sobe (`Exit 1`) | Variável de ambiente ausente no `.env` | `docker logs nova-alianca-local-api` → ver mensagem de erro |
| `Schema-validation: missing column` | Migration nova não aplicada | Verificar `flyway_schema_history` e logs da API |
| `Connection refused` no banco | Postgres não subiu ainda / porta errada | `docker compose ps`, aguardar healthcheck |
| KDS não recebe tickets | RabbitMQ inacessível | `docker logs rabbitmq-local`, verificar credenciais |
| Login falha | Redis indisponível (sessão) | `docker exec redis-local redis-cli ping` |
| Sync não funciona | URL online incorreta ou `require-https` bloqueando | `docker logs nova-alianca-local-api \| grep -i sync` |
| Disco cheio | Logs acumulados / imagens antigas | `docker system df`, `docker system prune -f` |
