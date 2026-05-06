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

O sistema usará o padrão **Outbox + Inbox + Retry** com um modelo evoluído para garantir máxima resiliência e rastreabilidade em ambientes instáveis.

### 5.1 Por que o modelo evoluído?

O modelo inicial foi expandido para suportar os desafios de uma operação híbrida local/online:

1.  **Idempotência Garantida (`idempotency_key`)**: Essencial para evitar duplicidade de pedidos ou vendas em caso de retentativas automáticas após falhas de conexão HTTPS.
2.  **Rastreabilidade por Agregado (`aggregate_type/id`)**: Facilita a auditoria e o rastreamento de todas as mudanças que ocorreram em um objeto de domínio específico (ex: um Pedido ou uma Categoria).
3.  **Clareza de Fluxo (`source/target_environment`)**: Define explicitamente quem gerou o evento e quem deve processá-lo, simplificando a lógica de roteamento no Monólito Modular.
4.  **Gestão de Falhas (`next_retry_at`, `last_error`)**: Permite monitoramento operacional detalhado e retentativas exponenciais baseadas no tempo, sem travar a fila de processamento.
5.  **Controle de Concorrência (`version`)**: Garante que atualizações concorrentes não sobrescrevam dados de forma inconsistente durante a sincronização.

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
    idempotency_key VARCHAR(120) NOT NULL UNIQUE,
    direction VARCHAR(40) NOT NULL,
    source_environment VARCHAR(40) NOT NULL,
    target_environment VARCHAR(40) NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID,
    event_type VARCHAR(120) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(40) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP,
    last_error TEXT,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0
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
| PENDING | Evento criado e aguardando processamento |
| PROCESSING | Evento em processamento |
| SYNCED | Evento sincronizado com sucesso |
| FAILED | Evento falhou e aguarda nova tentativa |
| RETRYING | Evento em fila de reprocessamento |
| IGNORED | Evento ignorado por regra de negócio |
| RECEIVED_BY_STORE | Evento online recebido e confirmado pela loja |
| DEAD_LETTER | Evento esgotou tentativas e exige intervenção |

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
Sync Worker local envia payload para API online
↓
API online valida idempotency_key e grava dados
↓
API online responde sucesso
↓
Sync Worker local marca evento como SYNCED e processed_at = NOW()
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

Tabela opcional (Inbox):
O modelo evoluído usa a própria tabela `sync_events` como Inbox no destino, validando a `idempotency_key` única.
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
```

Envia um evento da loja para o servidor online. Requer cabeçalhos `X-Store-ID`, `X-Signature` e `X-Idempotency-Key`.

### Online → Local por pull

```text
GET  /api/sync/pending?storeId=nova-alianca-001
POST /api/sync/events/{id}/ack
```

`GET /api/sync/pending` retorna eventos `PENDING` direção `ONLINE_TO_LOCAL` filtrados pelo `storeId`.
`POST /api/sync/events/{id}/ack` marca o evento como `RECEIVED_BY_STORE`.

### Administração (uso interno / backoffice)

```text
GET  /api/sync/events
GET  /api/sync/status
POST /api/sync/events/{id}/reprocess
POST /api/sync/events/{id}/ignore
```

- `GET /api/sync/events` — lista todos os eventos ordenados por data decrescente.
- `GET /api/sync/status` — retorna contagem de eventos por status (`PENDING`, `SYNCED`, `FAILED`, etc.).
- `POST /api/sync/events/{id}/reprocess` — reseta o status do evento para `PENDING`, reiniciando o processamento.
- `POST /api/sync/events/{id}/ignore` — descarta o evento com uma justificativa. Body: `{"reason": "texto"}`.

> **Nota:** Os endpoints `/events/batch`, `/events/{id}/status` e `/events/{id}/fail` não fazem parte da implementação. O envio em lote não existe — cada evento é enviado individualmente com chave de idempotência. Falha é gerenciada automaticamente pelo worker com retry exponencial.

## 21. Payload de evento

Exemplo:

```json
{
  "idempotencyKey": "order_12345_created_20260506",
  "direction": "LOCAL_TO_ONLINE",
  "sourceEnvironment": "LOCAL",
  "targetEnvironment": "ONLINE",
  "aggregateType": "ORDER",
  "aggregateId": "1c9e3817-63df-4cde-8479-21b16454d0cb",
  "eventType": "ORDER_CREATED",
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
