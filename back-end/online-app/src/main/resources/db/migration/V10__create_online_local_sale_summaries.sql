CREATE TABLE online_local_sale_summaries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id VARCHAR(80) NOT NULL,
    local_order_id UUID NOT NULL,
    order_number BIGINT,
    total_amount NUMERIC(12,2) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    finished_at TIMESTAMP,
    raw_payload JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_local_sale_summary_store_order UNIQUE (store_id, local_order_id)
);

CREATE INDEX idx_local_sale_summary_store_id
    ON online_local_sale_summaries(store_id);

CREATE INDEX idx_local_sale_summary_finished_at
    ON online_local_sale_summaries(finished_at);
