ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS delivery_address_id UUID REFERENCES customer_addresses(id);
