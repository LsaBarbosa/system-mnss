# Modelo de Domínio — Sistema Nova Aliança

## 1. Objetivo

Este documento descreve o modelo de domínio inicial do **Sistema Nova Aliança**.

O objetivo é mapear os principais conceitos de negócio:

- Produto
- Categoria
- Pedido
- Item do pedido
- Cliente
- Pagamento
- Caixa
- Movimentação de caixa
- KDS
- Estoque
- Sincronização
- Auditoria
- Usuários e permissões

## 2. Abordagem de modelagem

O sistema será organizado por domínios.

```text
Product
Order
Payment
Cash
KDS
Stock
Customer
Sync
Security
Audit
```

A arquitetura back-end deverá seguir uma organização modular, mantendo regras de negócio separadas de infraestrutura.

## 3. Domínios principais

## 3.1 Produto

Produto representa qualquer item vendido pela padaria.

Exemplos:

- Pão francês
- Pão doce
- Bolo
- Torta
- Salgado
- Lanche
- Bebida
- Café
- Produto sob encomenda

### Entidade: Product

Campos sugeridos:

```text
id
name
description
categoryId
price
promotionalPrice
costPrice
imageUrl
sku
barcode
unitType
preparationSector
preparationTimeMinutes
active
available
stockControlled
sellOnPdv
sellOnline
sellOnWhatsapp
createdAt
updatedAt
```

### Regras

- Produto inativo não aparece em nenhum canal.
- Produto indisponível não pode ser vendido online.
- Produto pode aparecer no PDV, mas não aparecer no site.
- Produto pode aparecer no WhatsApp, mas não no PDV.
- Produto pode ser vendido por unidade, peso, fatia ou encomenda.
- Produto pode ter preço promocional.
- Produto pode pertencer a um setor de preparo.

### Enum: UnitType

```java
public enum UnitType {
    UNIT,
    KG,
    GRAM,
    LITER,
    ML,
    SLICE,
    PORTION,
    PACKAGE
}
```

### Enum: PreparationSector

```java
public enum PreparationSector {
    BALCAO,
    CHAPA,
    BEBIDAS,
    CONFEITARIA,
    EXPEDICAO,
    DELIVERY,
    SEM_PREPARO
}
```

---

## 3.2 Categoria

Categoria organiza os produtos no PDV, site, WhatsApp e administração.

### Entidade: Category

Campos sugeridos:

```text
id
name
description
displayOrder
imageUrl
active
showOnline
showOnPdv
showOnWhatsapp
createdAt
updatedAt
```

### Regras

- Categoria inativa não deve aparecer para venda.
- Categoria pode estar visível em um canal e oculta em outro.
- Categoria deve ter ordenação para exibição.

---

## 3.3 Disponibilidade de produto

A disponibilidade representa se o produto pode ou não ser vendido.

### Entidade: ProductAvailability

Campos sugeridos:

```text
id
productId
status
availableQuantity
channel
reason
updatedBy
updatedAt
```

### Enum: AvailabilityStatus

```java
public enum AvailabilityStatus {
    AVAILABLE,
    UNAVAILABLE,
    AVAILABLE_UNTIL_STOCK_ENDS,
    PRE_ORDER_ONLY,
    PICKUP_ONLY,
    DELIVERY_ONLY,
    COUNTER_ONLY
}
```

### Enum: SalesChannel

```java
public enum SalesChannel {
    PDV,
    SITE,
    WHATSAPP,
    ALL
}
```

### Regras

- O ambiente local manda na disponibilidade real.
- Se o produto acabar na loja, deve ser marcado como indisponível.
- Produto indisponível localmente deve ser sincronizado com o online.
- Produtos sob encomenda podem continuar visíveis mesmo sem estoque.

---

## 3.4 Cliente

Cliente representa a pessoa que compra no site, WhatsApp ou presencialmente quando identificado.

### Entidade: Customer

Campos sugeridos:

```text
id
name
phone
email
document
birthDate
createdAt
updatedAt
```

### Regras

- Pedido online deve ter cliente.
- Pedido de balcão pode ser anônimo.
- Pedido de entrega precisa de telefone e endereço.
- Cliente pode ter múltiplos endereços.

### Entidade: CustomerAddress

Campos sugeridos:

```text
id
customerId
label
street
number
complement
neighborhood
city
state
zipCode
reference
latitude
longitude
defaultAddress
createdAt
updatedAt
```

---

## 3.5 Pedido

Pedido representa uma venda ou solicitação de preparo.

Pode nascer em:

- PDV
- Site
- WhatsApp
- Admin
- Balcão
- Atendimento manual

### Entidade: Order

Campos sugeridos:

```text
id
orderNumber
customerId
origin
status
paymentStatus
deliveryType
subtotal
discountAmount
deliveryFee
totalAmount
notes
createdAt
updatedAt
finishedAt
canceledAt
```

### Enum: OrderOrigin

```java
public enum OrderOrigin {
    PDV,
    SITE,
    WHATSAPP,
    ADMIN,
    MANUAL,
    INTEGRATION
}
```

### Enum: OrderStatus

```java
public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAID,
    SENT_TO_STORE,
    RECEIVED_BY_STORE,
    ACCEPTED,
    IN_PREPARATION,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FINISHED,
    CANCELED
}
```

### Enum: DeliveryType

```java
public enum DeliveryType {
    COUNTER,
    PICKUP,
    DELIVERY,
    LOCAL_CONSUMPTION
}
```

### Regras

- Pedido não pode ser finalizado sem itens.
- Pedido online deve ter cliente.
- Pedido de entrega deve ter endereço.
- Pedido pode ter itens de setores diferentes.
- Pedido só fica pronto quando todos os itens necessários estiverem prontos.
- Pedido cancelado deve registrar motivo e usuário.
- Pedido finalizado não deve permitir alteração simples.
- Pedido pago online deve preservar dados da transação.

---

## 3.6 Item do pedido

Representa cada produto dentro de um pedido.

### Entidade: OrderItem

Campos sugeridos:

```text
id
orderId
productId
productNameSnapshot
quantity
unitPrice
totalPrice
observation
status
preparationSector
createdAt
updatedAt
```

### Enum: OrderItemStatus

```java
public enum OrderItemStatus {
    CREATED,
    WAITING_PREPARATION,
    IN_PREPARATION,
    READY,
    DELIVERED,
    CANCELED
}
```

### Regras

- O item deve guardar snapshot do nome e preço do produto.
- Alterações futuras de preço não devem alterar pedidos antigos.
- Item com setor de preparo deve ir para o KDS.
- Item sem preparo pode ser finalizado diretamente.
- Item cancelado deve ajustar total, se a regra permitir.

---

## 3.7 Pagamento

Pagamento representa o recebimento de um pedido ou venda.

### Entidade: Payment

Campos sugeridos:

```text
id
orderId
method
status
amount
transactionId
gateway
paidAt
canceledAt
createdAt
updatedAt
```

### Enum: PaymentMethod

```java
public enum PaymentMethod {
    CASH,
    PIX,
    CREDIT_CARD,
    DEBIT_CARD,
    ONLINE_PIX,
    ONLINE_CREDIT_CARD,
    ONLINE_DEBIT_CARD,
    MEAL_VOUCHER,
    MIXED
}
```

### Enum: PaymentStatus

```java
public enum PaymentStatus {
    PENDING,
    AUTHORIZED,
    PAID,
    REFUSED,
    CANCELED,
    REFUNDED,
    EXPIRED
}
```

### Regras

- Um pedido pode ter pagamento misto.
- Pagamento online depende de confirmação do gateway.
- Pagamento presencial pode ser confirmado manualmente pelo operador.
- Pagamento em dinheiro pode acionar gaveta.
- Pagamento cancelado deve gerar log de auditoria.
- Pagamento online não deve ser confirmado apenas pelo front-end.

---

## 3.8 Caixa

Caixa representa o turno financeiro de uma estação/operador.

### Entidade: CashRegister

Campos sugeridos:

```text
id
operatorId
openedAt
closedAt
openingAmount
closingAmount
expectedAmount
differenceAmount
status
notes
createdAt
updatedAt
```

### Enum: CashRegisterStatus

```java
public enum CashRegisterStatus {
    OPEN,
    CLOSED,
    BLOCKED
}
```

### Regras

- Operador não pode vender sem caixa aberto, se a regra estiver ativa.
- Apenas um caixa pode estar aberto por operador/estação, conforme configuração.
- Caixa fechado não deve receber novas movimentações.
- Diferença de caixa deve ser registrada.
- Fechamento deve gerar resumo.

---

## 3.9 Movimentação de caixa

Representa entrada ou saída financeira no caixa.

### Entidade: CashMovement

Campos sugeridos:

```text
id
cashRegisterId
type
paymentMethod
amount
description
orderId
createdBy
createdAt
```

### Enum: CashMovementType

```java
public enum CashMovementType {
    SALE,
    CASH_IN,
    CASH_OUT,
    REFUND,
    ADJUSTMENT
}
```

### Regras

- Venda gera movimentação de entrada.
- Sangria gera saída.
- Suprimento gera entrada.
- Cancelamento ou estorno deve gerar movimentação apropriada.
- Toda movimentação crítica deve ter usuário responsável.

---

## 3.10 KDS

O KDS organiza a produção dos pedidos.

### Entidade: KdsTicket

Campos sugeridos:

```text
id
orderId
ticketNumber
sector
status
createdAt
startedAt
readyAt
finishedAt
```

### Enum: KdsTicketStatus

```java
public enum KdsTicketStatus {
    WAITING,
    IN_PREPARATION,
    READY,
    FINISHED,
    CANCELED
}
```

### Regras

- Pedido com itens de setores diferentes pode gerar tickets diferentes.
- Setor de chapa vê apenas itens da chapa.
- Setor de bebidas vê apenas bebidas.
- Expedição pode visualizar o pedido completo.
- Pedido fica pronto quando todos os tickets necessários estiverem prontos.

---

## 3.11 Estoque

Estoque controla quantidade e movimentação de produtos ou insumos.

### Entidade: StockMovement

Campos sugeridos:

```text
id
productId
type
quantity
reason
orderId
createdBy
createdAt
```

### Enum: StockMovementType

```java
public enum StockMovementType {
    IN,
    OUT,
    ADJUSTMENT,
    SALE,
    LOSS,
    RETURN
}
```

### Regras

- Venda pode gerar baixa de estoque.
- Ajuste manual deve registrar motivo.
- Produto com estoque zerado pode ficar indisponível automaticamente.
- Estoque físico é fonte local.
- Estoque online deve ser reflexo sincronizado.

---

## 3.12 Sincronização

Sincronização controla eventos entre local e online.

### Entidade: SyncEvent

Campos sugeridos:

```text
id
idempotencyKey
direction
sourceEnvironment
targetEnvironment
aggregateType
aggregateId
eventType
payload
status
retryCount
nextRetryAt
lastError
processedAt
createdAt
updatedAt
version
```

### Enum: SyncStatus

```java
public enum SyncStatus {
    PENDING,
    PROCESSING,
    SYNCED,
    FAILED,
    RETRYING,
    IGNORED,
    RECEIVED_BY_STORE,
    DEAD_LETTER
}
```

### Enum: SyncDirection

```java
public enum SyncDirection {
    LOCAL_TO_ONLINE,
    ONLINE_TO_LOCAL
}
```

### Enum: SyncEnvironment

```java
public enum SyncEnvironment {
    LOCAL,
    ONLINE
}
```

### Regras

- Toda alteração relevante gera evento.
- Evento deve ser idempotente.
- Evento com falha deve permitir retry.
- Evento sincronizado deve registrar data.
- Conflitos devem ser tratados por regra de domínio.

---

## 3.13 Usuário e segurança

### Entidade: User

Campos sugeridos:

```text
id
name
email
username
passwordHash
active
createdAt
updatedAt
```

### Entidade: Role

Campos sugeridos:

```text
id
name
description
```

### Perfis previstos

```text
ADMIN
GERENTE
CAIXA
ATENDENTE
COZINHA
EXPEDICAO
ENTREGADOR
CONSULTA
```

### Regras

- Usuário inativo não autentica.
- Ações críticas exigem permissão.
- Desconto, cancelamento e sangria devem gerar auditoria.
- Algumas ações podem exigir senha de gerente.

---

## 3.14 Auditoria

Auditoria registra operações críticas.

### Entidade: AuditLog

Campos sugeridos:

```text
id
userId
action
entityType
entityId
oldValue
newValue
ipAddress
createdAt
```

### Eventos auditáveis

- Login
- Cancelamento de venda
- Desconto
- Sangria
- Suprimento
- Fechamento de caixa
- Alteração de preço
- Alteração de disponibilidade
- Cancelamento de pedido
- Estorno

---

## 4. Agregados sugeridos

### Product Aggregate

```text
Product
ProductAvailability
StockMovement
```

### Order Aggregate

```text
Order
OrderItem
Payment
KdsTicket
```

### Cash Aggregate

```text
CashRegister
CashMovement
```

### Customer Aggregate

```text
Customer
CustomerAddress
```

### Sync Aggregate

```text
SyncEvent
SyncInbox
SyncOutbox
```

## 5. Regras transversais

- Toda entidade deve possuir `id`.
- IDs devem ser UUID.
- Tabelas devem possuir `created_at` e `updated_at`.
- Alterações críticas devem gerar auditoria.
- Valores monetários devem usar `BigDecimal`.
- Datas no back-end devem usar `Instant`, `LocalDateTime` ou `OffsetDateTime` conforme contexto.
- Status devem ser enums.
- Nunca depender do front-end para validar regra crítica.

## 6. Prioridade de implementação do domínio

1. Usuários e permissões
2. Categorias
3. Produtos
4. Disponibilidade
5. Pedidos
6. Itens do pedido
7. Pagamentos
8. Caixa
9. KDS
10. Sincronização
11. Estoque
12. Auditoria
