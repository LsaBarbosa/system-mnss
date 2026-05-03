# Sincronização — Sistema Nova Aliança

## 1. Objetivo

Este documento define a estratégia de sincronização entre o ambiente local da padaria e o ambiente online na VPS.

O sistema será híbrido:

```text
Local + Online
```

A sincronização deverá garantir que:

- Vendas presenciais sejam enviadas para a nuvem.
- Pedidos online sejam recebidos pela loja.
- Disponibilidade de produtos seja refletida no site/WhatsApp.
- Pagamentos online cheguem ao ambiente local.
- A loja continue funcionando mesmo sem internet.

## 2. Princípio central

> O sistema local não pode parar por falha de internet.

Logo, a sincronização deve ser assíncrona, resiliente e tolerante a falhas.

## 3. Decisão de comunicação

A comunicação entre local e online será feita por **HTTPS**.

Não será usado acesso direto entre bancos ou brokers.

Não expor na internet:

- PostgreSQL
- RabbitMQ
- Redis

Fluxo permitido:

```text
Local API / Sync Worker
↓ HTTPS
Online API / Sync Endpoint
```

## 4. Papel do RabbitMQ

Haverá RabbitMQ local e RabbitMQ online.

### RabbitMQ local

Usado para eventos internos da loja:

- Pedido criado no PDV
- Venda finalizada
- Produto indisponível
- Caixa fechado
- Pedido enviado para KDS
- Evento de sincronização pendente

### RabbitMQ online

Usado para eventos online:

- Pedido feito no site
- Pagamento aprovado
- Webhook recebido
- Mensagem WhatsApp recebida
- Pedido aguardando envio para loja

## 5. Padrão de sincronização

A sincronização usará o padrão:

```text
Outbox + Inbox + Retry
```

### Outbox

Registra eventos que precisam sair do ambiente atual.

### Inbox

Registra eventos recebidos para evitar duplicidade.

### Retry

Reprocessa eventos com falha.

## 6. Tabela principal

```sql
CREATE TABLE sync_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id UUID NOT NULL,
    payload JSONB NOT NULL,
    origin VARCHAR(30) NOT NULL,
    destination VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    synced_at TIMESTAMP
);
```

## 7. Status do evento

```text
PENDING
PROCESSING
SYNCED
FAILED
RETRYING
IGNORED
```

### Significado

| Status | Significado |
|---|---|
| PENDING | Evento criado e aguardando envio |
| PROCESSING | Evento em processamento |
| SYNCED | Evento sincronizado com sucesso |
| FAILED | Evento falhou |
| RETRYING | Evento será reprocessado |
| IGNORED | Evento ignorado por regra de negócio |

## 8. Tipos de eventos

Eventos previstos:

```text
ORDER_CREATED
ORDER_UPDATED
ORDER_CANCELED
ORDER_PAID
ORDER_SENT_TO_STORE
ORDER_RECEIVED_BY_STORE
ORDER_SENT_TO_KDS
ORDER_READY
ORDER_FINISHED
PAYMENT_CREATED
PAYMENT_APPROVED
PAYMENT_REFUSED
PAYMENT_EXPIRED
PRODUCT_CREATED
PRODUCT_UPDATED
PRODUCT_PRICE_CHANGED
PRODUCT_UNAVAILABLE
PRODUCT_AVAILABLE
STOCK_MOVED
CASH_REGISTER_OPENED
CASH_REGISTER_CLOSED
CASH_MOVEMENT_CREATED
```

## 9. Local para online

### 9.1 Exemplo: venda presencial

```text
Venda presencial realizada no PDV
↓
API local salva venda no PostgreSQL local
↓
API local cria sync_event ORDER_CREATED ou SALE_FINISHED
↓
Evento é publicado no RabbitMQ local
↓
Sync Worker local lê evento
↓
Sync Worker envia payload para API online
↓
API online valida e grava dados
↓
API online responde sucesso
↓
Sync Worker local marca evento como SYNCED
```

### 9.2 Dados enviados local → online

- Vendas presenciais
- Fechamentos de caixa
- Movimentações de caixa
- Atualização de disponibilidade
- Estoque
- Status de pedidos
- Logs críticos
- Pedidos finalizados

## 10. Online para local

### 10.1 Exemplo: pedido pelo site

```text
Cliente faz pedido no site
↓
API online cria pedido no PostgreSQL online
↓
API online cria sync_event ORDER_CREATED
↓
Pedido fica com status SENT_TO_STORE ou aguardando envio
↓
Sync Worker local consulta pedidos pendentes
↓
Local baixa pedido via API HTTPS
↓
Local grava pedido no PostgreSQL local
↓
Pedido aparece no PDV/KDS
↓
Local confirma recebimento
↓
Online atualiza status para RECEIVED_BY_STORE
```

### 10.2 Dados enviados online → local

- Pedidos do site
- Pedidos do WhatsApp
- Pagamentos online aprovados
- Pagamentos recusados
- Cancelamentos online
- Atualizações de cliente
- Alterações de cardápio, quando feitas online

## 11. Estratégia Pull vs Push

### Push

O ambiente local envia eventos para a nuvem.

```text
Local → Online
```

### Pull

O ambiente local consulta a nuvem em intervalos para buscar pendências.

```text
Local consulta Online
```

### Decisão recomendada

Usar os dois:

- **Push local → online** para vendas e eventos locais.
- **Pull local ← online** para buscar pedidos online.

Motivo:

- A VPS tem IP público.
- A loja pode estar atrás de NAT.
- O servidor local não precisa ficar exposto na internet.

## 12. Segurança da sincronização

A API de sincronização deve ter autenticação própria.

Opções:

- Token fixo por loja
- JWT técnico
- Assinatura HMAC
- Certificado mTLS em fase futura

### Recomendação inicial

Usar:

```text
Store ID
+
Client Secret
+
Assinatura HMAC do payload
```

Exemplo de headers:

```http
X-Store-Id: nova-alianca-001
X-Signature: hash_hmac_sha256(payload, secret)
X-Event-Id: uuid
X-Event-Timestamp: 2026-05-03T10:30:00Z
```

## 13. Idempotência

Todo evento deve ser idempotente.

Ou seja:

> Se o mesmo evento chegar duas vezes, o sistema não pode duplicar pedido, venda ou pagamento.

### Estratégia

- Todo evento deve ter UUID.
- Ambiente destino deve registrar evento recebido.
- Antes de processar, verificar se o evento já foi recebido.
- Se já foi processado, retornar sucesso sem reprocessar.

Tabela opcional:

```sql
CREATE TABLE sync_inbox (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    origin VARCHAR(30) NOT NULL,
    processed_at TIMESTAMP NOT NULL,
    payload JSONB NOT NULL
);
```

## 14. Retry

Eventos com falha devem ser reprocessados automaticamente.

### Estratégia inicial

```text
Tentativa 1: imediata
Tentativa 2: após 1 minuto
Tentativa 3: após 5 minutos
Tentativa 4: após 15 minutos
Tentativa 5: após 1 hora
```

Depois da quantidade máxima de tentativas:

```text
Status = FAILED
```

A falha deve aparecer no painel de sincronização.

## 15. Conflitos

Podem ocorrer conflitos entre local e online.

### 15.1 Produto indisponível

Caso:

```text
Produto está disponível online
Mas acabou na loja
```

Regra:

```text
Local manda na disponibilidade real.
```

Resultado:

- Local envia `PRODUCT_UNAVAILABLE`.
- Online remove ou bloqueia venda do produto.

### 15.2 Preço alterado

Caso:

```text
Preço alterado online e local ao mesmo tempo
```

Regra recomendada:

```text
Painel administrativo principal manda no preço.
```

No início, evitar alteração de preço em múltiplos lugares.

### 15.3 Pedido online pago, mas loja offline

Caso:

```text
Cliente pagou online
Loja está sem internet
```

Regra:

- Online deve manter pedido como `PAID`.
- Pedido fica aguardando confirmação da loja.
- Cliente pode ver status: "aguardando confirmação da loja".
- Quando loja voltar, pedido é sincronizado.

## 16. Status de pedidos online

Fluxo sugerido:

```text
CREATED
↓
PAYMENT_PENDING
↓
PAID
↓
SENT_TO_STORE
↓
RECEIVED_BY_STORE
↓
ACCEPTED
↓
IN_PREPARATION
↓
READY
↓
OUT_FOR_DELIVERY
↓
DELIVERED
↓
FINISHED
```

Status alternativo:

```text
CANCELED
```

## 17. Status de pedidos locais

Fluxo sugerido:

```text
CREATED
↓
ACCEPTED
↓
IN_PREPARATION
↓
READY
↓
FINISHED
```

Status alternativo:

```text
CANCELED
```

## 18. Painel de sincronização

O sistema deverá ter um painel para monitorar:

- Última sincronização
- Eventos pendentes
- Eventos com erro
- Pedidos online aguardando loja
- Pedidos locais aguardando envio
- Quantidade de retries
- Mensagem de erro
- Botão de reprocessar
- Botão de ignorar evento, com permissão de gerente/admin

## 19. Health checks

Verificações locais:

- PostgreSQL local online
- RabbitMQ local online
- Redis local online
- API local online
- Última sincronização com nuvem
- Quantidade de eventos pendentes
- Espaço em disco

Verificações online:

- PostgreSQL online
- RabbitMQ online
- Redis online
- API online
- Certificado HTTPS
- Webhooks de pagamento
- Eventos pendentes para lojas

## 20. Endpoints de sincronização

### Local → Online

```text
POST /api/sync/events
POST /api/sync/events/batch
GET  /api/sync/events/{id}/status
```

### Online → Local por pull

```text
GET  /api/sync/pending?storeId=nova-alianca-001
POST /api/sync/events/{id}/ack
POST /api/sync/events/{id}/fail
```

### Status

```text
GET /api/sync/status
GET /api/sync/health
```

## 21. Payload de evento

Exemplo:

```json
{
  "eventId": "1685f57e-3394-4094-8d4f-7cc0a89e9a66",
  "eventType": "ORDER_CREATED",
  "entityType": "ORDER",
  "entityId": "1c9e3817-63df-4cde-8479-21b16454d0cb",
  "origin": "LOCAL",
  "destination": "ONLINE",
  "occurredAt": "2026-05-03T10:30:00Z",
  "payload": {
    "orderNumber": 1521,
    "totalAmount": 48.90,
    "items": [
      {
        "productId": "a219d7ff-10d3-44b9-a802-38fdb9edb9e2",
        "productName": "X-Burger",
        "quantity": 1,
        "unitPrice": 22.90
      }
    ]
  }
}
```

## 22. Ordem de implementação

1. Criar tabela `sync_events`.
2. Criar serviço para registrar eventos.
3. Criar worker local de envio.
4. Criar endpoint online para receber eventos.
5. Criar idempotência.
6. Criar retry.
7. Criar pull de pedidos online.
8. Criar confirmação de recebimento.
9. Criar painel de sincronização.
10. Criar regras de conflito.

## 23. Decisões finais

- Sincronização será assíncrona.
- Local não depende da nuvem para vender.
- Comunicação local ↔ online via HTTPS.
- RabbitMQ não será público.
- Eventos devem ser idempotentes.
- Falhas devem gerar retry.
- Conflitos serão resolvidos por regra de domínio.
- Local manda na disponibilidade real.
- Local manda no estoque físico.
- Online manda em pedidos online e pagamentos online até serem recebidos pela loja.
