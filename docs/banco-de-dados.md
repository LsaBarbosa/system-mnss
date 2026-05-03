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
sync_events
audit_logs
```

## 6. Schema inicial sugerido

## 6.1 Roles

```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    customer_id UUID NOT NULL REFERENCES customers(id),
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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 6.9 Orders

```sql
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number BIGSERIAL UNIQUE,
    customer_id UUID REFERENCES customers(id),
    origin VARCHAR(40) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    delivery_type VARCHAR(50) NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    delivery_fee NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP,
    canceled_at TIMESTAMP
);
```

### Índices recomendados

```sql
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_origin ON orders(origin);
CREATE INDEX idx_orders_created_at ON orders(created_at);
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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    paid_at TIMESTAMP,
    canceled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Índices recomendados

```sql
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
```

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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 6.16 Stock Movements

```sql
CREATE TABLE stock_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id),
    type VARCHAR(50) NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    reason TEXT,
    order_id UUID REFERENCES orders(id),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 6.17 Sync Events

```sql
CREATE TABLE sync_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id UUID NOT NULL,
    payload JSONB NOT NULL,
    origin VARCHAR(30) NOT NULL,
    destination VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    synced_at TIMESTAMP
);
```

### Índices recomendados

```sql
CREATE INDEX idx_sync_events_status ON sync_events(status);
CREATE INDEX idx_sync_events_created_at ON sync_events(created_at);
CREATE INDEX idx_sync_events_entity ON sync_events(entity_type, entity_id);
```

## 6.18 Audit Logs

```sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(120) NOT NULL,
    entity_type VARCHAR(80),
    entity_id UUID,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(80),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 7. Flyway

Estrutura de migrations:

```text
resources/db/migration/
├── V001__create_roles.sql
├── V002__create_users.sql
├── V003__create_user_roles.sql
├── V004__create_categories.sql
├── V005__create_products.sql
├── V006__create_product_availability.sql
├── V007__create_customers.sql
├── V008__create_customer_addresses.sql
├── V009__create_orders.sql
├── V010__create_order_items.sql
├── V011__create_payments.sql
├── V012__create_cash_registers.sql
├── V013__create_cash_movements.sql
├── V014__create_kds_tickets.sql
├── V015__create_kds_ticket_items.sql
├── V016__create_stock_movements.sql
├── V017__create_sync_events.sql
└── V018__create_audit_logs.sql
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

## 9. Cuidados importantes

- Não usar `FLOAT` para dinheiro.
- Não sobrescrever preços antigos de pedidos.
- Salvar snapshot de produto no item do pedido.
- Não depender apenas do ID incremental para sincronização.
- Eventos devem ser idempotentes.
- Criar índices para consultas frequentes.
- Criar backup antes de migrations em produção.
- Evitar exclusão física de registros críticos.

## 10. Próximos passos

1. Criar migrations Flyway.
2. Criar entidades JPA.
3. Criar enums.
4. Criar repositories.
5. Criar seed inicial de roles.
6. Criar usuário admin inicial.
7. Criar testes de integração com Testcontainers.
