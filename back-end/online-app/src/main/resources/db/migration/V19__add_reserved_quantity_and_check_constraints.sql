-- stock_balances: add missing reserved_quantity column
ALTER TABLE stock_balances
    ADD COLUMN IF NOT EXISTS reserved_quantity NUMERIC(12,3) NOT NULL DEFAULT 0;

-- product_availability
ALTER TABLE product_availability
    ADD CONSTRAINT chk_product_availability_status
        CHECK (status IN ('AVAILABLE','UNAVAILABLE','AVAILABLE_UNTIL_STOCK_ENDS','PRE_ORDER_ONLY','PICKUP_ONLY','DELIVERY_ONLY','COUNTER_ONLY')),
    ADD CONSTRAINT chk_product_availability_channel
        CHECK (channel IN ('PDV','SITE','WHATSAPP','ALL'));

-- sync_events
ALTER TABLE sync_events
    ADD CONSTRAINT chk_sync_events_direction
        CHECK (direction IN ('LOCAL_TO_ONLINE','ONLINE_TO_LOCAL')),
    ADD CONSTRAINT chk_sync_events_source_env
        CHECK (source_environment IN ('LOCAL','ONLINE')),
    ADD CONSTRAINT chk_sync_events_target_env
        CHECK (target_environment IN ('LOCAL','ONLINE')),
    ADD CONSTRAINT chk_sync_events_status
        CHECK (status IN ('PENDING','PROCESSING','SYNCED','FAILED','RETRYING','DEAD_LETTER','IGNORED','RECEIVED_BY_STORE'));

-- products
ALTER TABLE products
    ADD CONSTRAINT chk_products_unit_type
        CHECK (unit_type IN ('UNIT','KG','GRAM','LITER','ML','SLICE','PORTION','PACKAGE')),
    ADD CONSTRAINT chk_products_preparation_sector
        CHECK (preparation_sector IN ('BALCAO','CHAPA','BEBIDAS','CONFEITARIA','EXPEDICAO','DELIVERY','SEM_PREPARO'));

-- orders (online-app uses ONLINE instead of SITE for origin)
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_origin
        CHECK (origin IN ('PDV','ONLINE','WHATSAPP')),
    ADD CONSTRAINT chk_orders_status
        CHECK (status IN ('CREATED','PAYMENT_PENDING','PAID','SENT_TO_STORE','RECEIVED_BY_STORE','ACCEPTED','IN_PREPARATION','READY','OUT_FOR_DELIVERY','DELIVERED','FINISHED','CANCELED')),
    ADD CONSTRAINT chk_orders_payment_status
        CHECK (payment_status IN ('PENDING','AUTHORIZED','PAID','PARTIALLY_PAID','REFUSED','CANCELED','REFUNDED','EXPIRED')),
    ADD CONSTRAINT chk_orders_delivery_type
        CHECK (delivery_type IN ('COUNTER','PICKUP','DELIVERY','LOCAL_CONSUMPTION')),
    ADD CONSTRAINT chk_orders_payment_method
        CHECK (payment_method IS NULL OR payment_method IN ('CASH','PIX','CREDIT_CARD','DEBIT_CARD','ONLINE_PIX','ONLINE_CREDIT_CARD','ONLINE_DEBIT_CARD','MEAL_VOUCHER','MIXED'));

-- order_items
ALTER TABLE order_items
    ADD CONSTRAINT chk_order_items_status
        CHECK (status IN ('CREATED','WAITING_PREPARATION','IN_PREPARATION','READY','DELIVERED','CANCELED')),
    ADD CONSTRAINT chk_order_items_preparation_sector
        CHECK (preparation_sector IN ('BALCAO','CHAPA','BEBIDAS','CONFEITARIA','EXPEDICAO','DELIVERY','SEM_PREPARO'));

-- payments
ALTER TABLE payments
    ADD CONSTRAINT chk_payments_method
        CHECK (method IN ('CASH','PIX','CREDIT_CARD','DEBIT_CARD','ONLINE_PIX','ONLINE_CREDIT_CARD','ONLINE_DEBIT_CARD','MEAL_VOUCHER','MIXED')),
    ADD CONSTRAINT chk_payments_status
        CHECK (status IN ('PENDING','AUTHORIZED','PAID','PARTIALLY_PAID','REFUSED','CANCELED','REFUNDED','EXPIRED'));

-- cash_registers
ALTER TABLE cash_registers
    ADD CONSTRAINT chk_cash_registers_status
        CHECK (status IN ('OPEN','CLOSED','BLOCKED'));

-- cash_movements
ALTER TABLE cash_movements
    ADD CONSTRAINT chk_cash_movements_type
        CHECK (type IN ('SALE','CASH_IN','CASH_OUT','REFUND','ADJUSTMENT')),
    ADD CONSTRAINT chk_cash_movements_payment_method
        CHECK (payment_method IS NULL OR payment_method IN ('CASH','PIX','CREDIT_CARD','DEBIT_CARD','ONLINE_PIX','ONLINE_CREDIT_CARD','ONLINE_DEBIT_CARD','MEAL_VOUCHER','MIXED'));

-- kds_tickets
ALTER TABLE kds_tickets
    ADD CONSTRAINT chk_kds_tickets_status
        CHECK (status IN ('WAITING','IN_PREPARATION','READY','FINISHED','CANCELED')),
    ADD CONSTRAINT chk_kds_tickets_sector
        CHECK (sector IN ('BALCAO','CHAPA','BEBIDAS','CONFEITARIA','EXPEDICAO','DELIVERY','SEM_PREPARO'));

-- kds_ticket_items
ALTER TABLE kds_ticket_items
    ADD CONSTRAINT chk_kds_ticket_items_status
        CHECK (status IN ('CREATED','WAITING_PREPARATION','IN_PREPARATION','READY','DELIVERED','CANCELED'));

-- stock_movements
ALTER TABLE stock_movements
    ADD CONSTRAINT chk_stock_movements_type
        CHECK (type IN ('IN','OUT','ADJUSTMENT','ADJUSTMENT_POSITIVE','ADJUSTMENT_NEGATIVE','INVENTORY_COUNT','SALE','LOSS','RETURN'));

-- whatsapp_conversations
ALTER TABLE whatsapp_conversations
    ADD CONSTRAINT chk_whatsapp_conversations_status
        CHECK (status IN ('OPEN','ASSIGNED','CLOSED'));

-- whatsapp_messages
ALTER TABLE whatsapp_messages
    ADD CONSTRAINT chk_whatsapp_messages_direction
        CHECK (direction IN ('INBOUND','OUTBOUND')),
    ADD CONSTRAINT chk_whatsapp_messages_status
        CHECK (status IN ('RECEIVED','SENT','FAILED'));

-- online_local_sale_summaries
ALTER TABLE online_local_sale_summaries
    ADD CONSTRAINT chk_local_sale_summaries_payment_status
        CHECK (payment_status IN ('PENDING','AUTHORIZED','PAID','PARTIALLY_PAID','REFUSED','CANCELED','REFUNDED','EXPIRED'));
