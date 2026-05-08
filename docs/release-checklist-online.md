# Release Checklist — Ambiente Online

Use este checklist antes de liberar uma nova versão para o ambiente online (produção).

## Pré-release — Build e validação

- [ ] `cd back-end && ./gradlew clean test` — todos os testes passam
- [ ] `cd back-end && ./gradlew :online-app:bootJar` — JAR gerado sem erros
- [ ] `cd front-end && npm ci && npm run lint && npm run format:check` — sem erros
- [ ] `cd front-end && npm test` — todos os testes passam
- [ ] `cd front-end && npm run build:online` — build sem erros
- [ ] `cd infra/online && cp .env.example .env && docker compose config` — compose válido (ajustar `.env` com valores reais antes)

## Segurança — Pré-deploy

- [ ] Todos os secrets em `.env` têm ≥ 32 bytes (`MNSS_STORE_001_SECRET`, `MNSS_PAYMENT_WEBHOOK_SECRET`, `MNSS_ONLINE_JWT_SECRET`, `SYNC_MASTER_SECRET`)
- [ ] `MNSS_AUTH_COOKIE_SECURE=true` configurado (HTTPS)
- [ ] `MNSS_SYNC_REQUIRE_HTTPS=true` configurado
- [ ] Nenhum secret com valor `change_me` em produção
- [ ] TLS/HTTPS configurado e certificado válido (`certbot`)

## Deploy

- [ ] Backup do banco antes do deploy:
  ```bash
  docker exec postgres-online pg_dump -U nova_alianca nova_alianca_online \
    -F c -f /backup/pre_release_$(date +%Y%m%d_%H%M%S).dump
  ```
- [ ] `cd infra/online && docker compose up -d --build`
- [ ] `docker compose ps` — todos os serviços `Up (healthy)`

## Smoke técnico

- [ ] `curl -s https://<dominio>/api/health | python3 -m json.tool` — status `UP`
- [ ] `curl -s https://<dominio>/actuator/health | python3 -m json.tool` — status `UP`
- [ ] `curl -s https://<dominio>/api/public/menu` — cardápio público retorna itens
- [ ] `curl -s https://<dominio>/api/public/info` — informações da loja

Ou rode o script:

```bash
BASE_URL=https://<dominio> infra/online/scripts/smoke-online.sh
```

## Smoke funcional — Pedido online

- [ ] Acessar site público (`https://<dominio>`)
- [ ] Visualizar cardápio público sem login
- [ ] Criar pedido online como cliente
- [ ] Pedido aparece no painel administrativo

## Webhook de pagamento (mock)

- [ ] Enviar webhook mock para `/api/public/payments/webhook` com assinatura válida
  ```bash
  PAYLOAD='{"event":"payment.approved","orderId":"test-123","amount":10.00}'
  SIG=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$MNSS_PAYMENT_WEBHOOK_SECRET" -binary | base64)
  curl -X POST https://<dominio>/api/public/payments/webhook \
    -H "Content-Type: application/json" \
    -H "X-Signature: $SIG" \
    -d "$PAYLOAD"
  ```
- [ ] Webhook com assinatura inválida retorna 401

## Webhook WhatsApp (mock)

- [ ] `GET /api/whatsapp/webhook?hub.mode=subscribe&hub.verify_token=<token>&hub.challenge=test` retorna `test`
- [ ] Webhook inválido retorna 403

## Sync local → online

- [ ] Enviar evento de sync com HMAC válido para `POST /api/sync/events`
- [ ] `GET /api/sync/pending` com HMAC válido retorna lista (pode estar vazia)
- [ ] `GET /api/sync/status` retorna contagens por status

## Verificação pós-deploy

- [ ] `docker compose logs nova-alianca-online-api | grep -i error` — sem erros críticos
- [ ] Migrations aplicadas corretamente: `flyway_schema_history`
- [ ] Nginx rota corretamente para API e frontend (sem 502)
- [ ] Logs não expõem secrets

## Rollback (se necessário)

```bash
# Restaurar backup
docker exec -i postgres-online pg_restore -U nova_alianca -d nova_alianca_online \
  /backup/pre_release_YYYYMMDD_HHMMSS.dump

# Subir versão anterior
git checkout <tag-anterior>
docker compose up -d --build
```
