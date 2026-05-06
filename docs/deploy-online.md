# Deploy Online — Sistema Nova Aliança

## 1. Objetivo

Este documento descreve o deploy online do Sistema Nova Aliança na VPS Hostinger.

O ambiente online será responsável por:

- Site institucional
- Cardápio online
- Pedidos online
- WhatsApp
- Chatbot
- Pagamentos online
- Webhooks
- Sincronização
- Relatórios remotos
- Backup online

## 2. Stack de infraestrutura

Stack decidida:

```text
Hostinger VPS
Ubuntu Server
Docker
Docker Compose
Nginx
Certbot / Let's Encrypt
PostgreSQL
RabbitMQ
Redis
Java Online API
Angular Site/Admin
```

## 3. Configuração da VPS

### Mínima aceitável

```text
2 vCPU
4 GB RAM
80 GB SSD
Ubuntu Server
Docker
```

### Recomendada

```text
4 vCPU
8 GB RAM
160 GB SSD ou mais
Ubuntu Server
Docker
Backup semanal
```

### Ideal para crescimento

```text
4 a 8 vCPU
16 GB RAM
SSD maior
Backup automático
Monitoramento
```

## 4. Domínios sugeridos

```text
padarianovaalianca.com.br
www.padarianovaalianca.com.br
api.padarianovaalianca.com.br
admin.padarianovaalianca.com.br
pedido.padarianovaalianca.com.br
```

## 5. Serviços online

Serviços previstos:

```text
nova-alianca-online-api
nova-alianca-site
nova-alianca-admin
postgres-online
rabbitmq-online
redis-online
nginx
certbot
```

> **Nota sobre sincronização:** Não há serviço `sync-worker-online` separado. O worker de processamento (`SyncInboxWorker`) roda embutido dentro do container `nova-alianca-online-api` via agendamento `@Scheduled`. Nenhum container adicional é necessário para sincronização.

## 6. Estrutura de diretórios na VPS

```text
/opt/nova-alianca-online/
├── docker-compose.yml
├── .env
├── nginx/
│   └── conf.d/
├── certbot/
│   ├── www/
│   └── conf/
├── postgres/
│   └── backup/
├── logs/
└── scripts/
    ├── backup-postgres.sh
    ├── update-system.sh
    └── renew-certificates.sh
```

## 7. Variáveis de ambiente

Arquivo `.env` online:

```env
SPRING_PROFILES_ACTIVE=online

POSTGRES_DB=nova_alianca_online
POSTGRES_USER=nova_alianca
POSTGRES_PASSWORD=change_me

RABBITMQ_DEFAULT_USER=nova_alianca
RABBITMQ_DEFAULT_PASS=change_me

REDIS_HOST=redis-online
REDIS_PORT=6379

JWT_SECRET=change_me
SYNC_MASTER_SECRET=change_me

SITE_URL=https://padarianovaalianca.com.br
API_URL=https://api.padarianovaalianca.com.br
ADMIN_URL=https://admin.padarianovaalianca.com.br
```

## 8. Docker Compose online

Exemplo:

```yaml
services:
  nova-alianca-online-api:
    image: nova-alianca/online-api:latest
    container_name: nova-alianca-online-api
    depends_on:
      - postgres-online
      - rabbitmq-online
      - redis-online
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}
      DB_HOST: postgres-online
      DB_PORT: 5432
      DB_NAME: ${POSTGRES_DB}
      DB_USER: ${POSTGRES_USER}
      DB_PASSWORD: ${POSTGRES_PASSWORD}
      RABBITMQ_HOST: rabbitmq-online
      RABBITMQ_USER: ${RABBITMQ_DEFAULT_USER}
      RABBITMQ_PASSWORD: ${RABBITMQ_DEFAULT_PASS}
      REDIS_HOST: redis-online
      JWT_SECRET: ${JWT_SECRET}
      SYNC_MASTER_SECRET: ${SYNC_MASTER_SECRET}
    expose:
      - "8080"
    restart: unless-stopped

  postgres-online:
    image: postgres:17
    container_name: postgres-online
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_online_data:/var/lib/postgresql/data
      - ./postgres/backup:/backup
    restart: unless-stopped

  rabbitmq-online:
    image: rabbitmq:4-management
    container_name: rabbitmq-online
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_DEFAULT_USER}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_DEFAULT_PASS}
    volumes:
      - rabbitmq_online_data:/var/lib/rabbitmq
    restart: unless-stopped

  redis-online:
    image: redis:8
    container_name: redis-online
    command: redis-server --appendonly yes
    volumes:
      - redis_online_data:/data
    restart: unless-stopped

  nginx:
    image: nginx:stable
    container_name: nginx
    depends_on:
      - nova-alianca-online-api
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/conf.d:/etc/nginx/conf.d
      - ./certbot/www:/var/www/certbot
      - ./certbot/conf:/etc/letsencrypt
    restart: unless-stopped

volumes:
  postgres_online_data:
  rabbitmq_online_data:
  redis_online_data:
```

## 9. Nginx para API

Arquivo:

```text
nginx/conf.d/api.conf
```

Configuração HTTP inicial:

```nginx
server {
    listen 80;
    server_name api.padarianovaalianca.com.br;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        proxy_pass http://nova-alianca-online-api:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Configuração HTTPS:

```nginx
server {
    listen 443 ssl;
    server_name api.padarianovaalianca.com.br;

    ssl_certificate /etc/letsencrypt/live/api.padarianovaalianca.com.br/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.padarianovaalianca.com.br/privkey.pem;

    location / {
        proxy_pass http://nova-alianca-online-api:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 10. Certificado SSL

Usar Certbot/Let's Encrypt.

Comando exemplo:

```bash
docker run --rm \
  -v ./certbot/conf:/etc/letsencrypt \
  -v ./certbot/www:/var/www/certbot \
  certbot/certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email contato@padarianovaalianca.com.br \
  --agree-tos \
  --no-eff-email \
  -d api.padarianovaalianca.com.br
```

## 11. Firewall

Usar UFW.

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

Portas que não devem ficar públicas:

```text
5432 PostgreSQL
5672 RabbitMQ
15672 RabbitMQ Management
6379 Redis
```

## 12. SSH

Recomendações:

- Usar chave SSH.
- Desabilitar login root por senha.
- Usar usuário administrativo.
- Limitar acesso à porta 22, se possível.

Configuração recomendada em `/etc/ssh/sshd_config`:

```text
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes
```

Reiniciar SSH:

```bash
sudo systemctl restart ssh
```

## 13. Backup online

Script básico:

```bash
#!/bin/bash

BACKUP_DIR="/opt/nova-alianca-online/postgres/backup"
DATE=$(date +"%Y-%m-%d_%H-%M-%S")
CONTAINER="postgres-online"
DB="nova_alianca_online"
USER="nova_alianca"

mkdir -p $BACKUP_DIR

docker exec $CONTAINER pg_dump -U $USER $DB > "$BACKUP_DIR/backup_$DATE.sql"

find $BACKUP_DIR -type f -name "*.sql" -mtime +15 -delete
```

Agendar no cron:

```bash
0 3 * * * /opt/nova-alianca-online/scripts/backup-postgres.sh
```

## 14. Atualização online

Script:

```bash
#!/bin/bash

cd /opt/nova-alianca-online

docker compose pull
docker compose up -d
docker image prune -f
```

Arquivo:

```text
/opt/nova-alianca-online/scripts/update-system.sh
```

## 15. CI/CD

Pipeline recomendado:

```text
Push na branch main
↓
Build back-end
↓
Testes
↓
Build imagem Docker
↓
Push no registry
↓
SSH na VPS
↓
docker compose pull
↓
docker compose up -d
```

## 16. Health checks

Endpoints:

```text
GET /actuator/health
GET /actuator/metrics
GET /api/sync/status
```

Monitorar:

- API online
- PostgreSQL
- RabbitMQ
- Redis
- Certificado HTTPS
- Espaço em disco
- Eventos pendentes
- Webhooks
- Último backup

## 17. Logs

No início:

```bash
docker compose logs -f
docker logs -f nova-alianca-online-api
```

Futuro:

- Prometheus
- Grafana
- Loki
- Promtail

## 18. Webhooks

O ambiente online receberá webhooks de:

- Gateway de pagamento
- WhatsApp
- Outros canais externos

### Regras

- Validar assinatura do webhook.
- Nunca confiar apenas no payload.
- Registrar payload bruto para auditoria.
- Processar em fila.
- Responder rápido ao provedor.
- Processar regra de negócio de forma assíncrona.

## 19. Endpoints públicos

Exemplos:

```text
GET  /api/public/menu
GET  /api/public/products
POST /api/orders
POST /api/payments/webhook
POST /api/whatsapp/webhook
POST /api/sync/events
GET  /api/sync/pending
```

## 20. Segurança online

Medidas obrigatórias:

- HTTPS
- Firewall
- JWT
- Rate limit
- Validação de webhook
- Banco privado
- Redis privado
- RabbitMQ privado
- Backups
- Logs de auditoria
- Monitoramento

## 21. Processo inicial de deploy

```text
1. Criar VPS na Hostinger.
2. Apontar DNS para IP da VPS.
3. Instalar Docker.
4. Instalar Docker Compose.
5. Configurar firewall.
6. Configurar SSH seguro.
7. Criar diretório /opt/nova-alianca-online.
8. Criar .env.
9. Criar docker-compose.yml.
10. Criar Nginx.
11. Subir containers.
12. Emitir certificado SSL.
13. Testar API.
14. Testar site.
15. Configurar backup.
16. Configurar monitoramento inicial.
```

## 22. Checklist online

```text
[ ] VPS criada
[ ] DNS apontado
[ ] Ubuntu atualizado
[ ] Docker instalado
[ ] Docker Compose instalado
[ ] Firewall configurado
[ ] SSH seguro
[ ] Diretório do projeto criado
[ ] .env configurado
[ ] Docker Compose criado
[ ] Nginx configurado
[ ] API online subiu
[ ] PostgreSQL online saudável
[ ] RabbitMQ online saudável
[ ] Redis online saudável
[ ] HTTPS configurado
[ ] Backup configurado
[ ] Webhook de pagamento testado
[ ] Webhook WhatsApp testado
[ ] Endpoint de sync testado
```
