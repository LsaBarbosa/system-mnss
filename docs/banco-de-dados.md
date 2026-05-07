# Banco de Dados — Sistema Nova Aliança

## 1. Objetivo

Este documento define a estrutura inicial de banco de dados do **Sistema Nova Aliança**.

O sistema terá dois bancos PostgreSQL:

```text
PostgreSQL Local
PostgreSQL Online
```

O banco local será usado para operação da padaria.

O banco online será usado para site, WhatsApp, pagamentos, pedidos online e relatórios remotos.

## 2. Decisões principais

- Banco principal: PostgreSQL.
- Versionamento de schema: Flyway.
- IDs principais: UUID.
- Valores monetários: `NUMERIC(12,2)`.
- Datas de criação/alteração: `TIMESTAMP`.
- Payloads de sincronização: `JSONB`.
- Banco local e online podem compartilhar boa parte do schema.
- Algumas tabelas terão uso principal local.
- Outras terão uso principal online.

## 3. Convenções

### 3.1 Nome de tabelas

Usar `snake_case`.

Exemplos:

```text
products
order_items
cash_registers
sync_events
```

### 3.2 Nome de colunas

Usar `snake_case`.

Exemplos:

```text
created_at
updated_at
product_id
order_status
```

### 3.3 Chaves primárias

Todas as tabelas principais devem usar UUID.

```sql
id UUID PRIMARY KEY
```

### 3.4 Campos padrão

Entidades principais devem possuir:

```sql
created_at TIMESTAMP NOT NULL
updated_at TIMESTAMP NOT NULL
```

Entidades com inativação lógica devem possuir:

```sql
active BOOLEAN NOT NULL DEFAULT TRUE
```

## 4. Extensões PostgreSQL

Recomenda-se habilitar:

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
```

Para UUID:

```sql
gen_random_uuid()
```

## 5. Tabelas iniciais

Tabelas previstas:

```text
users
roles
user_roles
categories
products
product_availability
customers
customer_addresses
orders
order_items
payments
cash_registers
cash_movements
kds_tickets
kds_ticket_items
stock_movements
stock_balances
sync_events
audit_logs
online_local_sale_summaries
whatsapp_conversations
whatsapp_messages
```

## 6. Schema consolidado

Os blocos abaixo representam o schema final após as migrations Flyway atuais.

Entidades JPA que estendem `BaseEntity` devem possuir:

```sql
version BIGINT NOT NULL DEFAULT 0
```

## 6.1 Roles

```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

## 6.2 Users

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

## 6.3 User Roles

```sql
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id),
    role_id UUID NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);
```

## 6.4 Categories

```sql
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL DEFAULT 0,
    image_url TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    show_online BOOLEAN NOT NULL DEFAULT TRUE,
    show_on_pdv BOOLEAN NOT NULL DEFAULT TRUE,
    show_on_whatsapp BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

## 6.5 Products

```sql
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID REFERENCES categories(id),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(12,2) NOT NULL,
    promotional_price NUMERIC(12,2),
    cost_price NUMERIC(12,2),
    sku VARCHAR(80),
    barcode VARCHAR(80),
    image_url TEXT,
    unit_type VARCHAR(40) NOT NULL,
    preparation_sector VARCHAR(60) NOT NULL,
    preparation_time_minutes INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    sell_on_pdv BOOLEAN NOT NULL DEFAULT TRUE,
    sell_online BOOLEAN NOT NULL DEFAULT TRUE,
    sell_on_whatsapp BOOLEAN NOT NULL DEFAULT TRUE,
    stock_controlled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

### Índices recomendados

```sql
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_products_barcode ON products(barcode);
```

## 6.6 Product Availability

```sql
CREATE TABLE product_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id),
    status VARCHAR(60) NOT NULL,
    available_quantity NUMERIC(12,3),
    channel VARCHAR(40) NOT NULL,
    reason TEXT,
    updated_by UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

## 6.7 Customers

```sql
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(150),
    document VARCHAR(30),
    birth_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

### Índices recomendados

```sql
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_email ON customers(email);
```

## 6.8 Customer Addresses

```sql
CREATE TABLE customer_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID REFERENCES customers(id),
    label VARCHAR(80),
    street VARCHAR(150) NOT NULL,
    number VARCHAR(30),
    complement VARCHAR(120),
    neighborhood VARCHAR(120),
    city VARCHAR(120),
    state VARCHAR(60),
    zip_code VARCHAR(20),
    reference TEXT,
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    default_address BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

No banco local, `customer_id` é opcional a partir de `V8__make_customer_address_customer_nullable.sql`
para permitir endereços recebidos por sincronização sem cliente local. No banco online, o vínculo com
cliente continua obrigatório na criação de endereço online.

## 6.9 Orders

```sql
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number BIGSERIAL UNIQUE,
    customer_id UUID REFERENCES customers(id),
    delivery_address_id UUID REFERENCES customer_addresses(id),
    origin VARCHAR(40) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    delivery_type VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    delivery_fee NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    finished_at TIMESTAMP,
    canceled_at TIMESTAMP
);
```

### Índices recomendados

```sql
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_delivery_address_id ON orders(delivery_address_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_origin ON orders(origin);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_payment_method ON orders(payment_method);
```

## 6.10 Order Items

```sql
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id),
    product_id UUID REFERENCES products(id),
    product_name_snapshot VARCHAR(150) NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    total_price NUMERIC(12,2) NOT NULL,
    observation TEXT,
    status VARCHAR(50) NOT NULL,
    preparation_sector VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

### Índices recomendados

```sql
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_status ON order_items(status);
```

## 6.11 Payments

```sql
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id),
    method VARCHAR(60) NOT NULL,
    status VARCHAR(60) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    transaction_id VARCHAR(150),
    gateway VARCHAR(80),
    webhook_payload TEXT,
    paid_at TIMESTAMP,
    canceled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

### Índices recomendados

```sql
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
```

`webhook_payload` existe no banco online para auditoria de webhooks de pagamento. O banco local não
usa essa coluna atualmente.

## 6.12 Cash Registers

```sql
CREATE TABLE cash_registers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operator_id UUID NOT NULL REFERENCES users(id),
    opened_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    opening_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    closing_amount NUMERIC(12,2),
    expected_amount NUMERIC(12,2),
    difference_amount NUMERIC(12,2),
    status VARCHAR(40) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

## 6.13 Cash Movements

```sql
CREATE TABLE cash_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cash_register_id UUID NOT NULL REFERENCES cash_registers(id),
    type VARCHAR(50) NOT NULL,
    payment_method VARCHAR(60),
    amount NUMERIC(12,2) NOT NULL,
    description TEXT,
    order_id UUID REFERENCES orders(id),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

## 6.14 KDS Tickets

```sql
CREATE TABLE kds_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id),
    ticket_number BIGSERIAL UNIQUE,
    sector VARCHAR(60) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    started_at TIMESTAMP,
    ready_at TIMESTAMP,
    finished_at TIMESTAMP
);
```

## 6.15 KDS Ticket Items

```sql
CREATE TABLE kds_ticket_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    kds_ticket_id UUID NOT NULL REFERENCES kds_tickets(id),
    order_item_id UUID NOT NULL REFERENCES order_items(id),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

## 6.16 Stock Movements

```sql
CREATE TABLE stock_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id),
    type VARCHAR(50) NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    previous_quantity NUMERIC(12,3),
    resulting_quantity NUMERIC(12,3),
    reason TEXT,
    order_id UUID REFERENCES orders(id),
    created_by UUID REFERENCES users(id),
    source VARCHAR(50),
    reference_type VARCHAR(50),
    reference_id UUID,
    idempotency_key VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

### Índices recomendados

```sql
CREATE INDEX idx_stock_movements_product_id ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_order_id ON stock_movements(order_id);
CREATE UNIQUE INDEX idx_stock_movements_idempotency_key
    ON stock_movements(idempotency_key) WHERE idempotency_key IS NOT NULL;
```

Os campos `previous_quantity`, `resulting_quantity`, `source`, `reference_type`, `reference_id` e
`idempotency_key` existem no banco local. No banco online, `stock_movements` permanece como tabela
operacional herdada do schema inicial e recebe apenas `version` para compatibilidade futura.

## 6.17 Stock Balances

```sql
CREATE TABLE stock_balances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL UNIQUE REFERENCES products(id),
    quantity NUMERIC(12,3) NOT NULL DEFAULT 0,
    reserved_quantity NUMERIC(12,3) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

### Índices recomendados

```sql
CREATE INDEX idx_stock_balances_product_id ON stock_balances(product_id);
```

No banco online, `stock_balances` não possui `reserved_quantity` atualmente. O local mantém a reserva
para proteger a operação presencial e o online mantém apenas o saldo consolidado recebido por sync.

## 6.18 Sync Events

```sql
CREATE TABLE sync_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
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
    version BIGINT NOT NULL DEFAULT 0
);
```

### Índices recomendados

```sql
CREATE INDEX idx_sync_events_status ON sync_events(status);
CREATE INDEX idx_sync_events_next_retry_at ON sync_events(next_retry_at);
CREATE INDEX idx_sync_events_aggregate ON sync_events(aggregate_type, aggregate_id);
CREATE INDEX idx_sync_events_status_direction ON sync_events(status, direction);
```

## 6.19 Audit Logs

```sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id UUID REFERENCES users(id),
    action VARCHAR(120) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id UUID,
    details JSONB,
    ip_address VARCHAR(80),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

## 6.20 Online Local Sale Summaries

Tabela online usada para consolidar eventos `SALE_FINISHED` recebidos do ambiente local.

```sql
CREATE TABLE online_local_sale_summaries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id VARCHAR(80) NOT NULL,
    local_order_id UUID NOT NULL,
    order_number BIGINT,
    total_amount NUMERIC(12,2) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    finished_at TIMESTAMP,
    raw_payload JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_local_sale_summary_store_order UNIQUE (store_id, local_order_id)
);
```

### Índices recomendados

```sql
CREATE INDEX idx_local_sale_summary_store_id ON online_local_sale_summaries(store_id);
CREATE INDEX idx_local_sale_summary_finished_at ON online_local_sale_summaries(finished_at);
```

## 6.21 WhatsApp Conversations

Tabelas online usadas pelo fluxo de atendimento e pedidos via WhatsApp.

```sql
CREATE TABLE whatsapp_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_phone VARCHAR(30) NOT NULL,
    customer_name VARCHAR(150),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    assigned_to UUID REFERENCES users(id),
    order_id UUID REFERENCES orders(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

### Índices recomendados

```sql
CREATE INDEX idx_whatsapp_conversations_phone ON whatsapp_conversations(customer_phone);
CREATE INDEX idx_whatsapp_conversations_status ON whatsapp_conversations(status);
```

## 6.22 WhatsApp Messages

```sql
CREATE TABLE whatsapp_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES whatsapp_conversations(id),
    external_message_id VARCHAR(120) UNIQUE,
    direction VARCHAR(20) NOT NULL,
    sender_phone VARCHAR(30),
    sender_name VARCHAR(150),
    content TEXT,
    message_type VARCHAR(30) NOT NULL DEFAULT 'TEXT',
    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

### Índices recomendados

```sql
CREATE INDEX idx_whatsapp_messages_conversation ON whatsapp_messages(conversation_id);
```

`external_message_id` possui constraint `UNIQUE`; o PostgreSQL já mantém índice para essa constraint.

## 7. Flyway

Estrutura de migrations:

```text
back-end/local-app/src/main/resources/db/migration/
├── V1__baseline_schema.sql
├── V2__core_business_schema.sql
├── V3__seed_initial_roles.sql
├── V4__sync_outbox_schema.sql
├── V5__add_stock_controlled_to_products.sql
├── V6__add_delivery_address_to_orders.sql
├── V7__add_index_orders_delivery_address_id.sql
├── V8__make_customer_address_customer_nullable.sql
├── V9__create_stock_balances.sql
└── V10__expand_stock_movements.sql

back-end/online-app/src/main/resources/db/migration/
├── V1__baseline_schema.sql
├── V2__core_business_schema.sql
├── V3__seed_initial_roles.sql
├── V4__whatsapp_schema.sql
├── V5__add_stock_controlled_to_products.sql
├── V6__add_version_to_categories.sql
├── V7__add_version_to_online_tables.sql
├── V8__add_payment_method_to_orders.sql
├── V9__add_webhook_payload_to_payments.sql
├── V10__create_online_local_sale_summaries.sql
├── V11__add_delivery_address_to_orders.sql
├── V12__create_stock_balances.sql
├── V13__enforce_payment_method_on_orders.sql
├── V14__add_version_to_online_operational_tables.sql
├── V15__add_fk_whatsapp_conversations_assigned_to.sql
└── V16__drop_redundant_whatsapp_external_message_index.sql
```

## 8. Separação local e online

### 8.1 Tabelas críticas locais

- orders
- order_items
- payments
- cash_registers
- cash_movements
- kds_tickets
- kds_ticket_items
- stock_movements
- stock_balances
- sync_events
- audit_logs

### 8.2 Tabelas críticas online

- products
- categories
- customers
- customer_addresses
- orders
- order_items
- payments
- sync_events
- audit_logs
- online_local_sale_summaries
- stock_balances
- whatsapp_conversations
- whatsapp_messages

> As tabelas `cash_registers`, `cash_movements`, `kds_tickets`, `kds_ticket_items` e `stock_movements`
> também existem no banco online por compartilhamento inicial de schema. Elas não são a fonte primária
> da operação local, mas recebem `version` para manter compatibilidade com entidades baseadas em `BaseEntity`
> caso sejam mapeadas futuramente.

### 8.3 Tabelas compartilhadas

- users
- roles
- categories
- products
- product_availability
- orders
- order_items
- payments
- sync_events
- stock_balances

## 9. Cuidados importantes

- Não usar `FLOAT` para dinheiro.
- Não sobrescrever preços antigos de pedidos.
- Salvar snapshot de produto no item do pedido.
- Não depender apenas do ID incremental para sincronização.
- Eventos devem ser idempotentes.
- Criar índices para consultas frequentes.
- Criar backup antes de migrations em produção.
- Evitar exclusão física de registros críticos.

## 10. Política de data/hora

A aplicação deve gravar datas em UTC.

As colunas `TIMESTAMP` devem ser interpretadas como UTC pela aplicação.

Não usar horário local de máquina para regra de negócio.

## 11. Próximos passos

1. Criar migrations Flyway.
2. Criar entidades JPA.
3. Criar enums.
4. Criar repositories.
5. Criar seed inicial de roles.
6. Criar usuário admin inicial.
7. Criar testes de integração com Testcontainers.
