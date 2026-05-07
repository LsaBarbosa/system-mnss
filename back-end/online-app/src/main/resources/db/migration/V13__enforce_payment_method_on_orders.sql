-- Enforce mandatory payment method in online orders.
-- Legacy records created before strict validation may have null/blank values.
UPDATE orders
SET payment_method = CASE
    WHEN origin = 'WHATSAPP' THEN 'CASH'
    ELSE 'ONLINE_PIX'
END
WHERE payment_method IS NULL
   OR BTRIM(payment_method) = '';

ALTER TABLE orders
    ALTER COLUMN payment_method TYPE VARCHAR(50),
    ALTER COLUMN payment_method SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_orders_payment_method
    ON orders(payment_method);
