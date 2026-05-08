# Deploy Local — Sistema Nova Aliança

## 1. Objetivo

Este documento descreve como deve ser estruturado o deploy local do Sistema Nova Aliança dentro da padaria.

O ambiente local será responsável por:

- PDV
- KDS
- Caixa
- Impressão
- Banco local
- RabbitMQ local
- Redis local
- Sincronização com a nuvem

## 2. Decisão principal

O deploy local usará:

```text
Ubuntu Server
Docker
Docker Compose
Nginx
PostgreSQL
RabbitMQ
Redis
Java Local API
Angular PDV/KDS/Admin
```

## 3. Servidor local

### Configuração mínima

```text
Mini PC ou desktop compacto
Intel i5 ou Ryzen 5
16 GB RAM
SSD 512 GB
Rede Gigabit
Ubuntu Server
Nobreak
```

### Configuração ideal

```text
Intel i5/i7 ou Ryzen 5/7
32 GB RAM
SSD NVMe 1 TB
Segundo disco para backup
Rede Gigabit
Ubuntu Server
Nobreak
```

## 4. Serviços locais

Serviços previstos:

```text
nova-alianca-local-api
nova-alianca-local-front
postgres-local
rabbitmq-local
redis-local
nginx-local
```

> **Nota sobre sincronização:** Não há serviço `sync-worker-local` separado. O worker de sincronização (`SyncOutboxWorker`) roda embutido dentro do container `nova-alianca-local-api` via agendamento `@Scheduled`. Nenhum container adicional é necessário para sincronização.

Opcionalmente:

```text
prometheus-local
grafana-local
loki-local
```

## 5. Estrutura de diretórios no servidor

Sugestão:

```text
/opt/nova-alianca/
├── docker-compose.yml
├── .env
├── nginx/
│   └── conf.d/
├── postgres/
│   ├── data/
│   └── backup/
├── rabbitmq/
├── redis/
├── logs/
└── scripts/
    ├── backup-postgres.sh
    ├── restore-postgres.sh
    └── update-system.sh
```

## 6. Variáveis de ambiente

Copie o exemplo e edite os segredos **antes** de subir os containers:

```bash
cd infra/local
cp .env.example .env
# edite .env e substitua todos os valores "change_me"
docker compose config   # valida sintaxe e variáveis
```

Variáveis obrigatórias (o Docker Compose injeta as corretas no container da API):

| Variável | Descrição |
|---|---|
| `POSTGRES_DB` | Nome do banco PostgreSQL |
| `POSTGRES_USER` | Usuário do banco |
| `POSTGRES_PASSWORD` | Senha do banco (mín. 16 chars) |
| `RABBITMQ_DEFAULT_USER` | Usuário do RabbitMQ |
| `RABBITMQ_DEFAULT_PASS` | Senha do RabbitMQ (mín. 16 chars) |
| `AUTH_TOKEN_SECRET` | Segredo JWT (mín. 32 chars) |
| `MNSS_INITIAL_ADMIN_PASSWORD` | Senha do admin inicial |
| `MNSS_STORE_ID` | Identificador da loja — **deve ser idêntico** ao `MNSS_DEFAULT_STORE_ID` do ambiente online (`store-001`) |
| `MNSS_STORE_SECRET` | Segredo HMAC da loja (mín. 32 chars) — **deve ser idêntico** ao `MNSS_STORE_001_SECRET` do ambiente online |
| `MNSS_SYNC_REQUIRE_HTTPS` | `true` em produção, `false` só em dev HTTP |
| `MNSS_CORS_ALLOWED_ORIGINS` | Origins CORS permitidas (separadas por vírgula). Default: `http://localhost,http://127.0.0.1` |
| `MNSS_HSTS_ENABLED` | Deve ficar `false` no HTTP local. HSTS só deve ser habilitado em HTTPS real. |

> **Importante:** O Compose local já monta as variáveis nos nomes que o Spring lê
> (`MNSS_LOCAL_DB_URL`, `MNSS_LOCAL_RABBITMQ_HOST`, etc.). Não altere os nomes
> das variáveis no `docker-compose.yml`.

## 7. Docker Compose local

Exemplo inicial:

```yaml
services:
  nova-alianca-local-api:
    image: nova-alianca/local-api:latest
    container_name: nova-alianca-local-api
    depends_on:
      - postgres-local
      - rabbitmq-local
      - redis-local
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}
      DB_HOST: postgres-local
      DB_PORT: 5432
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      RABBITMQ_HOST: rabbitmq-local
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_DEFAULT_USER}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_DEFAULT_PASS}
      REDIS_HOST: redis-local
      AUTH_TOKEN_SECRET: ${AUTH_TOKEN_SECRET}
      AUTH_TOKEN_TTL: ${AUTH_TOKEN_TTL}
      MNSS_INITIAL_ADMIN_USERNAME: ${MNSS_INITIAL_ADMIN_USERNAME}
      MNSS_INITIAL_ADMIN_PASSWORD: ${MNSS_INITIAL_ADMIN_PASSWORD}
      MNSS_INITIAL_ADMIN_EMAIL: ${MNSS_INITIAL_ADMIN_EMAIL}
      MNSS_ONLINE_URL: ${MNSS_ONLINE_URL}
      MNSS_STORE_ID: ${MNSS_STORE_ID}
      MNSS_STORE_SECRET: ${MNSS_STORE_SECRET}
    expose:
      - "8080"
    restart: unless-stopped

  nova-alianca-local-front:
    image: nova-alianca/local-front:latest
    container_name: nova-alianca-local-front
    depends_on:
      - nova-alianca-local-api
    restart: unless-stopped

  postgres-local:
    image: postgres:17
    container_name: postgres-local
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_local_data:/var/lib/postgresql/data
      - ./postgres/backup:/backup
    restart: unless-stopped

  rabbitmq-local:
    image: rabbitmq:4-management
    container_name: rabbitmq-local
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_DEFAULT_USER}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_DEFAULT_PASS}
    volumes:
      - rabbitmq_local_data:/var/lib/rabbitmq
    restart: unless-stopped

  redis-local:
    image: redis:8
    container_name: redis-local
    command: redis-server --appendonly yes
    volumes:
      - redis_local_data:/data
    restart: unless-stopped

  nginx-local:
    image: nginx:stable
    container_name: nginx-local
    depends_on:
      - nova-alianca-local-api
      - nova-alianca-local-front
    ports:
      - "80:80"
    volumes:
      - ./nginx/conf.d:/etc/nginx/conf.d
    restart: unless-stopped

volumes:
  postgres_local_data:
  rabbitmq_local_data:
  redis_local_data:
```

## 8. Nginx local

> **Nota:** No ambiente local o padrão inicial é HTTP interno na rede da padaria. HTTPS com certificado local é opcional e pode ser adotado posteriormente.

Arquivo:

```text
/opt/nova-alianca/nginx/conf.d/local.conf
```

Exemplo:

```nginx
server {
    listen 80;
    server_name api.novaalianca.local;

    location / {
        proxy_pass http://nova-alianca-local-api:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

Para simplificar no início, o PDV pode acessar por IP:

```text
http://192.168.0.10
```

## 9. DNS local

Opções:

### Simples

Usar IP fixo do servidor local.

```text
http://192.168.0.10
```

### Melhor

Configurar DNS local no roteador:

```text
pdv.novaalianca.local
kds.novaalianca.local
admin.novaalianca.local
api.novaalianca.local
```

## 10. IP fixo

O servidor local deve ter IP fixo.

Exemplo:

```text
192.168.0.10
```

Reservar IP no roteador ou configurar no Ubuntu Server.

## 11. Firewall local

No servidor local, liberar apenas o necessário:

```bash
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw enable
```

Não expor publicamente:

```text
5432 PostgreSQL
5672 RabbitMQ
15672 RabbitMQ Management
6379 Redis
```

## 12. Backup local

Script sugerido:

```bash
#!/bin/bash

BACKUP_DIR="/opt/nova-alianca/postgres/backup"
DATE=$(date +"%Y-%m-%d_%H-%M-%S")
CONTAINER="postgres-local"
DB="${POSTGRES_DB:-nova_alianca_local}"
USER="${POSTGRES_USER:-nova_alianca}"

mkdir -p "$BACKUP_DIR"

docker exec "$CONTAINER" pg_dump -U "$USER" "$DB" > "$BACKUP_DIR/backup_$DATE.sql"

find "$BACKUP_DIR" -type f -name "*.sql" -mtime +15 -delete
```

Arquivo:

```text
/opt/nova-alianca/scripts/backup-postgres.sh
```

Agendar no cron:

```bash
0 2 * * * /opt/nova-alianca/scripts/backup-postgres.sh
```

## 13. Atualização local

Script inicial:

```bash
#!/bin/bash

cd /opt/nova-alianca

docker compose pull
docker compose up -d
docker image prune -f
```

Arquivo:

```text
/opt/nova-alianca/scripts/update-system.sh
```

## 14. Processo de deploy local inicial

```bash
# 1. Clonar ou copiar o repositório para o servidor
cd infra/local

# 2. Configurar variáveis de ambiente
cp .env.example .env
# editar segredos no .env

# 3. Validar sintaxe do Compose
docker compose config

# 4. Subir ambiente (a API aguarda banco/RabbitMQ/Redis ficarem healthy)
docker compose up -d --build

# 5. Acompanhar inicialização
docker compose ps
docker logs -f nova-alianca-local-api

# 6. Executar smoke de infra e PDV
bash scripts/smoke-local-infra.sh
bash scripts/smoke-local-pdv.sh
```

> A API só inicia após PostgreSQL, RabbitMQ e Redis estarem healthy
> (`condition: service_healthy`). Em caso de falha, verifique
> `docker compose ps` para ver qual serviço não ficou healthy.

## 15. Comandos principais

Subir ambiente:

```bash
docker compose up -d
```

Ver logs:

```bash
docker compose logs -f
```

Ver logs da API:

```bash
docker logs -f nova-alianca-local-api
```

Parar ambiente:

```bash
docker compose down
```

Atualizar:

```bash
docker compose pull
docker compose up -d
```

## 16. Health checks

Endpoints:

```text
GET /actuator/health
GET /actuator/metrics
GET /api/sync/health
```

Verificar:

- API local
- PostgreSQL
- RabbitMQ
- Redis
- Espaço em disco
- Última sincronização
- Eventos pendentes

## 17. Impressão local

A API local ou módulo de impressão precisará acessar impressora local.

Opções:

- Impressora USB no próprio PDV.
- Impressora Ethernet na rede.
- Serviço local de impressão.

Preferência:

```text
Impressora Ethernet
```

## 18. Segurança local

Regras:

- Servidor protegido fisicamente.
- Senhas fortes.
- SSH com chave, se possível.
- Banco não exposto fora do Docker/rede interna.
- Backups diários.
- Nobreak.
- Usuários com permissões mínimas.

## 19. Contingência

### Internet caiu

Sistema local continua.

### Servidor local caiu

A operação é impactada.

Mitigações:

- Nobreak
- Backup
- Documentação de restauração
- Equipamento reserva futuro

### Banco corrompido

Restaurar último backup.

### Atualização falhou

Manter imagem anterior disponível.

## 20. Checklist de implantação local

```text
[ ] Servidor instalado
[ ] IP fixo configurado
[ ] Docker instalado
[ ] Docker Compose instalado
[ ] Diretório /opt/nova-alianca criado
[ ] .env configurado
[ ] docker-compose.yml criado
[ ] Nginx local configurado
[ ] Containers iniciados
[ ] API local respondendo
[ ] PostgreSQL saudável
[ ] RabbitMQ saudável
[ ] Redis saudável
[ ] PDV acessando sistema
[ ] KDS acessando sistema
[ ] Impressora testada
[ ] Gaveta testada
[ ] Backup configurado
[ ] Nobreak instalado
[ ] Sincronização testada
```
