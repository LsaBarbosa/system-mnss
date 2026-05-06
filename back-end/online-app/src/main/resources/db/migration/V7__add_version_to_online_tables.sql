DO $$
DECLARE
    table_name TEXT;
BEGIN
    FOREACH table_name IN ARRAY ARRAY[
        'categories',
        'products',
        'product_availability',
        'customers',
        'customer_addresses',
        'orders',
        'order_items',
        'payments',
        'sync_events',
        'whatsapp_conversations',
        'whatsapp_messages'
    ]
    LOOP
        IF to_regclass(table_name) IS NOT NULL THEN
            EXECUTE format(
                'ALTER TABLE %I ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0',
                table_name
            );
        END IF;
    END LOOP;
END $$;
