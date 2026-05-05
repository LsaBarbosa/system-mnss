-- V4__sync_outbox_schema.sql
-- Add version column for optimistic locking (BaseEntity @Version)
-- to tables that were created before this column was introduced.

ALTER TABLE sync_events ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE categories ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE product_availability ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE customer_addresses ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE cash_registers ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE cash_movements ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE kds_tickets ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE kds_ticket_items ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE stock_movements ADD COLUMN IF NOT EXISTS version BIGINT;

-- Add better index for outbox worker queries
CREATE INDEX IF NOT EXISTS idx_sync_events_status_direction ON sync_events(status, direction);
