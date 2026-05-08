# Roteiro de Homologação — Operação Local Offline

Este roteiro valida que a loja continua vendendo sem acesso à internet.

## Pré-condições

- Ambiente local rodando: `docker compose ps` mostra todos os serviços `Up (healthy)`.
- Pelo menos um produto cadastrado com estoque disponível.
- Caixa pode estar aberto ou fechado (o roteiro inclui a abertura).
- Acesso à UI local via browser: `http://localhost` (ou a porta configurada).

## Como simular ausência de internet

### Opção 1 — Bloquear saída do container da API (recomendado para teste isolado)

```bash
# Bloqueia saída da API para a internet, mantendo rede Docker interna
docker exec nova-alianca-local-api sh -c "
  ip route del default || true
"
```

Para restaurar:

```bash
docker restart nova-alianca-local-api
```

### Opção 2 — Desconectar a interface de rede externa do host

Desligar o cabo de rede ou desativar o Wi-Fi do servidor/laptop onde o Docker roda.
A rede Docker interna continua funcionando normalmente.

---

## Passos do roteiro

### 1. Verificar serviços locais

```bash
docker compose ps
```

Resultado esperado: todos os serviços com status `Up` e os healthchecks como `(healthy)`.

### 2. Simular ausência de internet

Execute um dos métodos acima. Confirme com:

```bash
# A partir do container da API, isso deve falhar
docker exec nova-alianca-local-api curl -fsS --max-time 5 https://google.com
# Esperado: erro de conexão (exit code != 0)
```

### 3. Acessar a interface local

Abra `http://localhost` no browser. A página deve carregar normalmente a partir do nginx local.

### 4. Fazer login

- Usuário: `admin` (ou o usuário configurado em `MNSS_INITIAL_ADMIN_USERNAME`)
- Senha: definida em `MNSS_INITIAL_ADMIN_PASSWORD`

Resultado esperado: login bem-sucedido, dashboard carrega.

### 5. Abrir caixa

- Navegue até **Caixa → Abrir Caixa**.
- Informe o valor inicial.
- Confirme abertura.

Resultado esperado: caixa aberto, status `OPEN`.

### 6. Criar venda no PDV

- Navegue até **PDV**.
- Clique em **Nova Venda**.

### 7. Adicionar produto sem preparo (balcão)

- Busque e adicione um produto com setor `SEM_PREPARO`.

Resultado esperado: item adicionado à venda, total atualizado.

### 8. Adicionar produto com preparo (ex: chapa)

- Adicione um produto com setor diferente de `SEM_PREPARO` (ex: `CHAPA`).

Resultado esperado: item adicionado, venda com dois itens.

### 9. Registrar pagamento

- Clique em **Pagar**.
- Escolha o método (ex: Dinheiro).
- Informe o valor.
- Confirme.

Resultado esperado: pagamento registrado, troco calculado se dinheiro.

### 10. Finalizar venda

- Confirme finalização.

Resultado esperado: venda finalizada com status `PAID` ou `SENT_TO_STORE`.

### 11. Verificar movimento de caixa

- Navegue até **Caixa → Movimentos**.

Resultado esperado: movimento de venda registrado com o valor e método corretos.

### 12. Verificar ticket no KDS

- Acesse o KDS (Tela de produção).

Resultado esperado: ticket do produto com preparo aparece na fila do setor correspondente.

### 13. Verificar evento pendente de sincronização

```bash
# Via API local
curl -s http://localhost/api/health | python3 -m json.tool

# Via banco local
docker exec postgres-local psql -U nova_alianca -d nova_alianca_local \
  -c "SELECT status, count(*) FROM sync_events GROUP BY status;"
```

Resultado esperado: linha com `PENDING` (ou `RETRYING`) — o worker tentará enviar mas falhará por falta de internet.

### 14. Reconectar internet

Restaure a conectividade (veja Opção de simulação acima).

### 15. Verificar envio automático do evento

Aguarde até 60 segundos (intervalo padrão do worker) e então:

```bash
docker exec postgres-local psql -U nova_alianca -d nova_alianca_local \
  -c "SELECT status, count(*) FROM sync_events GROUP BY status;"
```

Resultado esperado: evento passa para `SYNCED`.

Logs da API:

```bash
docker logs nova-alianca-local-api --tail=50 | grep -i sync
```

---

## Critérios de aprovação

| Passo | Critério |
|-------|---------|
| 4 | Login bem-sucedido sem internet |
| 5 | Caixa aberto localmente |
| 10 | Venda finalizada localmente |
| 11 | Movimento de caixa registrado |
| 12 | Ticket KDS criado |
| 13 | Evento de sync pendente (não perdido) |
| 15 | Evento sincronizado após reconexão |

---

## Falhas comuns e diagnóstico

### PDV não carrega

```bash
docker compose ps
docker logs nginx-local --tail=20
docker logs nova-alianca-local-api --tail=50
```

### Caixa não abre / venda não finaliza

```bash
docker logs nova-alianca-local-api --tail=100 | grep -i error
```

### Evento não sincroniza após reconexão

```bash
# Verificar configuração de sync
docker exec nova-alianca-local-api env | grep MNSS_SYNC

# Verificar logs do worker
docker logs nova-alianca-local-api --tail=100 | grep -i "sync\|outbox"
```

### KDS não recebe ticket

```bash
docker logs nova-alianca-local-api --tail=50 | grep -i "kds\|rabbitmq"
docker logs rabbitmq-local --tail=20
```
