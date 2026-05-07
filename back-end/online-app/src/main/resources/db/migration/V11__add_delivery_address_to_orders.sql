ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS delivery_address_id UUID REFERENCES customer_addresses(id);

CREATE INDEX IF NOT EXISTS idx_orders_delivery_address_id ON orders(delivery_address_id);
