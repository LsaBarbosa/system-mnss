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
nova-alianca-frontend
postgres-online
rabbitmq-online
redis-online
nginx
certbot
```

> **Nota sobre o frontend:** `nova-alianca-frontend` é um único container Angular que serve tanto o site público (`padarianovaalianca.com.br`) quanto o painel admin (`admin.padarianovaalianca.com.br`). O roteamento entre os dois domínios é feito pelo Nginx; o Angular Router trata as rotas internas de cada contexto.

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

Copie o exemplo e edite **todos** os segredos antes de subir os containers:

```bash
cd infra/online
cp .env.example .env
# edite .env e substitua todos os valores "change_me"
docker compose config   # valida sintaxe
```

> **Atenção:** Não versione o arquivo `.env` real. O `.env.example` já contém
> todas as variáveis necessárias com valores fictícios seguros.

Segredos obrigatórios (mínimo 32 caracteres):

| Variável | Descrição |
|---|---|
| `POSTGRES_PASSWORD` | Senha do banco PostgreSQL |
| `MNSS_ONLINE_JWT_SECRET` | Segredo JWT para sessões web |
| `SYNC_MASTER_SECRET` | Segredo mestre de sincronização |
| `MNSS_STORE_001_SECRET` | Segredo HMAC da loja física |
| `MNSS_PAYMENT_WEBHOOK_SECRET` | Segredo do webhook de pagamento |
| `WHATSAPP_VERIFY_TOKEN` | Token de verificação do WhatsApp Business |
| `MNSS_CORS_ALLOWED_ORIGINS` | Origins CORS permitidas (separadas por vírgula). Default: `https://padarianovaalianca.com.br,https://admin.padarianovaalianca.com.br` |
| `MNSS_HSTS_ENABLED` | Default `true`. Use `true` somente quando HTTPS/TLS estiver pronto no domínio final; use `false` em homologação HTTP. |

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
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      RABBITMQ_HOST: rabbitmq-online
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_DEFAULT_USER}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_DEFAULT_PASS}
      REDIS_HOST: redis-online
      JWT_SECRET: ${JWT_SECRET}
      SYNC_MASTER_SECRET: ${SYNC_MASTER_SECRET}
      MNSS_DEFAULT_STORE_ID: ${MNSS_DEFAULT_STORE_ID}
      MNSS_STORE_001_SECRET: ${MNSS_STORE_001_SECRET}
      MNSS_PAYMENT_WEBHOOK_SECRET: ${MNSS_PAYMENT_WEBHOOK_SECRET}
      WHATSAPP_VERIFY_TOKEN: ${WHATSAPP_VERIFY_TOKEN}
      WHATSAPP_PROVIDER: ${WHATSAPP_PROVIDER}
      SPRING_SECURITY_USER_NAME: ${SPRING_SECURITY_USER_NAME}
      SPRING_SECURITY_USER_PASSWORD: ${SPRING_SECURITY_USER_PASSWORD}
    expose:
      - "8080"
    restart: unless-stopped

  nova-alianca-frontend:
    image: nova-alianca/frontend:latest
    container_name: nova-alianca-frontend
    depends_on:
      - nova-alianca-online-api
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
      - nova-alianca-frontend
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
DB="${POSTGRES_DB:-nova_alianca_online}"
USER="${POSTGRES_USER:-nova_alianca}"

mkdir -p "$BACKUP_DIR"

docker exec "$CONTAINER" pg_dump -U "$USER" "$DB" > "$BACKUP_DIR/backup_$DATE.sql"

find "$BACKUP_DIR" -type f -name "*.sql" -mtime +15 -delete
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

## 16. Health checks e smoke test

Após o deploy, execute o smoke test para validar os endpoints básicos:

```bash
BASE_URL=https://api.padarianovaalianca.com.br ./scripts/smoke-online.sh
```

Endpoints verificados:

```text
GET /api/health
GET /api/version
GET /api/public/info
GET /api/public/menu
GET /actuator/health
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
GET  /api/public/info
POST /api/public/orders
POST /api/public/payments/online
POST /api/public/payments/webhook
POST /api/auth/login
POST /api/whatsapp/webhook
POST /api/sync/events
GET  /api/sync/pending
```

Endpoints internos (exigem autenticação HTTP Basic ou Bearer JWT):

```text
GET  /api/auth/me
GET  /api/whatsapp/catalog
GET  /api/whatsapp/conversations
POST /api/whatsapp/orders
GET  /api/sync/events
GET  /api/sync/status
POST /api/sync/events/{id}/reprocess
POST /api/sync/events/{id}/ignore
```

## 20. Segurança online

Medidas obrigatórias:

- HTTPS com certificado válido (Let's Encrypt)
- Firewall restritivo (porta 22 SSH, porta 80/443 HTTP/HTTPS)
- CORS configurável por `MNSS_CORS_ALLOWED_ORIGINS`
- Headers de segurança (X-Frame-Options, X-Content-Type-Options, HSTS configurável por `MNSS_HSTS_ENABLED`, Referrer-Policy)
- Assinatura HMAC e segredos técnicos para sync/webhooks
- HTTP Basic e/ou Bearer JWT nos endpoints internos de `/api/**` que não são públicos
- JWT para painéis administrativos quando o módulo de autenticação online estiver habilitado
- Rate limit (recomendado via Nginx ou WAF)
- Validação de webhook
- Banco privado (não expor 5432)
- Redis privado (não expor 6379)
- RabbitMQ privado (não expor 5672 ou 15672 publicamente)
- Backups automáticos e verificados
- Logs de auditoria
- Monitoramento de performance e segurança

### Headers de segurança

A API online inclui automaticamente:

```
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Referrer-Policy: strict-origin-when-cross-origin
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

Nginx também pode adicionar headers complementares:

```nginx
add_header X-Frame-Options "DENY" always;
add_header X-Content-Type-Options "nosniff" always;
add_header Referrer-Policy "strict-origin-when-cross-origin" always;
add_header Permissions-Policy "geolocation=(), microphone=(), camera=()" always;
```

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
