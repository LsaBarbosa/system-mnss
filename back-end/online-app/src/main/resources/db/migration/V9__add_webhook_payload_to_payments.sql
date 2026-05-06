ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS webhook_payload TEXT;
