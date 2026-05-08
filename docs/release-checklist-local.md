# Release Checklist — Ambiente Local

Use este checklist antes de liberar uma nova versão para o ambiente local da padaria.

## Pré-release — Build e validação

- [ ] `cd back-end && ./gradlew clean test` — todos os testes passam
- [ ] `cd back-end && ./gradlew :local-app:bootJar` — JAR gerado sem erros
- [ ] `cd front-end && npm ci && npm run lint && npm run format:check` — sem erros de lint/formatação
- [ ] `cd front-end && npm test` — todos os testes passam
- [ ] `cd front-end && npm run build:local` — build sem erros
- [ ] `cd infra/local && cp .env.example .env && docker compose config` — compose válido

## Deploy

- [ ] Backup do banco antes do deploy:
  ```bash
  docker exec postgres-local pg_dump -U nova_alianca nova_alianca_local \
    -F c -f /backup/pre_release_$(date +%Y%m%d_%H%M%S).dump
  ```
- [ ] `cd infra/local && docker compose pull` (se usando imagens externas)
- [ ] `cd infra/local && docker compose up -d --build`
- [ ] `docker compose ps` — todos os serviços `Up (healthy)`

## Smoke técnico

- [ ] `curl -s http://localhost/api/health | python3 -m json.tool` — status UP
- [ ] `curl -s http://localhost/api/version` — versão correta
- [ ] `curl -s http://localhost/api/public/info` — informações da loja
- [ ] `curl -s http://localhost/api/public/menu` — cardápio público

Ou rode o script:

```bash
BASE_URL=http://localhost infra/local/scripts/smoke-local.sh
```

## Smoke funcional — PDV

- [ ] Login na interface web (`http://localhost`)
- [ ] Abrir caixa com valor inicial
- [ ] Criar venda com produto sem preparo
- [ ] Criar venda com produto com preparo (verifica KDS)
- [ ] Registrar pagamento em dinheiro (verifica troco)
- [ ] Finalizar venda — status `PAID` ou `SENT_TO_STORE`
- [ ] Verificar movimento de caixa registrado
- [ ] Verificar ticket no KDS para produto com preparo
- [ ] Verificar evento de sync em `sync_events` (status `PENDING` ou `SYNCED`)

## Estoque

- [ ] Produto com `stockControlled=true`: venda desconta saldo
- [ ] Produto com `stockControlled=false`: venda não bloqueia por saldo insuficiente
- [ ] Ajuste manual com motivo registra `stock_movements`

## Verificação pós-deploy

- [ ] `docker compose logs nova-alianca-local-api | grep -i error` — sem erros críticos
- [ ] Migrations aplicadas: verificar `flyway_schema_history` no banco
- [ ] Sem erros no log do nginx: `docker logs nginx-local`
- [ ] Reconectar internet (se simulou offline) e verificar sync

## Rollback (se necessário)

```bash
# Restaurar backup
docker exec -i postgres-local pg_restore -U nova_alianca -d nova_alianca_local \
  /backup/pre_release_YYYYMMDD_HHMMSS.dump

# Subir versão anterior
git checkout <tag-anterior>
docker compose up -d --build
```
