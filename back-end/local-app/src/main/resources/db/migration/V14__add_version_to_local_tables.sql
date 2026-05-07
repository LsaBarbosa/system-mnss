-- Adiciona coluna version para controle de concorrência otimista (Hibernate @Version)
-- Utiliza ADD COLUMN IF NOT EXISTS para ser idempotente em bancos que já possuam a coluna.
DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOREACH tbl IN ARRAY ARRAY[
        'roles',
        'users',
        'categories',
        'products',
        'product_availability',
        'customers',
        'customer_addresses',
        'orders',
        'order_items',
        'payments',
        'cash_registers',
        'cash_movements',
        'kds_tickets',
        'kds_ticket_items',
        'stock_movements',
        'stock_balances',
        'sync_events',
        'audit_logs'
    ]
    LOOP
        IF to_regclass(tbl) IS NOT NULL THEN
            EXECUTE format(
                'ALTER TABLE %I ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0',
                tbl
            );
        END IF;
    END LOOP;
END $$;
