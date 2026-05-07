-- Delivery addresses created from online order sync have no local customer
ALTER TABLE customer_addresses
    ALTER COLUMN customer_id DROP NOT NULL;
