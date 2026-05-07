-- user_roles
ALTER TABLE user_roles
    DROP CONSTRAINT user_roles_user_id_fkey,
    ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE user_roles
    DROP CONSTRAINT user_roles_role_id_fkey,
    ADD CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;

-- products
ALTER TABLE products
    DROP CONSTRAINT products_category_id_fkey,
    ADD CONSTRAINT products_category_id_fkey FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;

-- product_availability
ALTER TABLE product_availability
    DROP CONSTRAINT product_availability_product_id_fkey,
    ADD CONSTRAINT product_availability_product_id_fkey FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;
ALTER TABLE product_availability
    DROP CONSTRAINT product_availability_updated_by_fkey,
    ADD CONSTRAINT product_availability_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

-- audit_logs
ALTER TABLE audit_logs
    DROP CONSTRAINT audit_logs_actor_user_id_fkey,
    ADD CONSTRAINT audit_logs_actor_user_id_fkey FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL;

-- customer_addresses
ALTER TABLE customer_addresses
    DROP CONSTRAINT customer_addresses_customer_id_fkey,
    ADD CONSTRAINT customer_addresses_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE;

-- orders
ALTER TABLE orders
    DROP CONSTRAINT orders_customer_id_fkey,
    ADD CONSTRAINT orders_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL;
ALTER TABLE orders
    DROP CONSTRAINT orders_delivery_address_id_fkey,
    ADD CONSTRAINT orders_delivery_address_id_fkey FOREIGN KEY (delivery_address_id) REFERENCES customer_addresses(id) ON DELETE SET NULL;

-- order_items
ALTER TABLE order_items
    DROP CONSTRAINT order_items_order_id_fkey,
    ADD CONSTRAINT order_items_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;
ALTER TABLE order_items
    DROP CONSTRAINT order_items_product_id_fkey,
    ADD CONSTRAINT order_items_product_id_fkey FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL;

-- payments
ALTER TABLE payments
    DROP CONSTRAINT payments_order_id_fkey,
    ADD CONSTRAINT payments_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;

-- cash_registers
ALTER TABLE cash_registers
    DROP CONSTRAINT cash_registers_operator_id_fkey,
    ADD CONSTRAINT cash_registers_operator_id_fkey FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE RESTRICT;

-- cash_movements
ALTER TABLE cash_movements
    DROP CONSTRAINT cash_movements_cash_register_id_fkey,
    ADD CONSTRAINT cash_movements_cash_register_id_fkey FOREIGN KEY (cash_register_id) REFERENCES cash_registers(id) ON DELETE CASCADE;
ALTER TABLE cash_movements
    DROP CONSTRAINT cash_movements_order_id_fkey,
    ADD CONSTRAINT cash_movements_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL;
ALTER TABLE cash_movements
    DROP CONSTRAINT cash_movements_created_by_fkey,
    ADD CONSTRAINT cash_movements_created_by_fkey FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

-- kds_tickets
ALTER TABLE kds_tickets
    DROP CONSTRAINT kds_tickets_order_id_fkey,
    ADD CONSTRAINT kds_tickets_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;

-- kds_ticket_items
ALTER TABLE kds_ticket_items
    DROP CONSTRAINT kds_ticket_items_kds_ticket_id_fkey,
    ADD CONSTRAINT kds_ticket_items_kds_ticket_id_fkey FOREIGN KEY (kds_ticket_id) REFERENCES kds_tickets(id) ON DELETE CASCADE;
ALTER TABLE kds_ticket_items
    DROP CONSTRAINT kds_ticket_items_order_item_id_fkey,
    ADD CONSTRAINT kds_ticket_items_order_item_id_fkey FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE;

-- stock_movements
ALTER TABLE stock_movements
    DROP CONSTRAINT stock_movements_product_id_fkey,
    ADD CONSTRAINT stock_movements_product_id_fkey FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT;
ALTER TABLE stock_movements
    DROP CONSTRAINT stock_movements_order_id_fkey,
    ADD CONSTRAINT stock_movements_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL;
ALTER TABLE stock_movements
    DROP CONSTRAINT stock_movements_created_by_fkey,
    ADD CONSTRAINT stock_movements_created_by_fkey FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

-- stock_balances
ALTER TABLE stock_balances
    DROP CONSTRAINT stock_balances_product_id_fkey,
    ADD CONSTRAINT stock_balances_product_id_fkey FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

-- whatsapp_conversations
ALTER TABLE whatsapp_conversations
    DROP CONSTRAINT whatsapp_conversations_order_id_fkey,
    ADD CONSTRAINT whatsapp_conversations_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL;
ALTER TABLE whatsapp_conversations
    DROP CONSTRAINT fk_whatsapp_conversations_assigned_to,
    ADD CONSTRAINT fk_whatsapp_conversations_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL;

-- whatsapp_messages
ALTER TABLE whatsapp_messages
    DROP CONSTRAINT whatsapp_messages_conversation_id_fkey,
    ADD CONSTRAINT whatsapp_messages_conversation_id_fkey FOREIGN KEY (conversation_id) REFERENCES whatsapp_conversations(id) ON DELETE CASCADE;
