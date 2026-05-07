ALTER TABLE stock_movements
    ADD COLUMN IF NOT EXISTS previous_quantity NUMERIC(12,3),
    ADD COLUMN IF NOT EXISTS resulting_quantity NUMERIC(12,3),
    ADD COLUMN IF NOT EXISTS source VARCHAR(50),
    ADD COLUMN IF NOT EXISTS reference_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS reference_id UUID,
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS idx_stock_movements_idempotency_key
    ON stock_movements(idempotency_key) WHERE idempotency_key IS NOT NULL;
