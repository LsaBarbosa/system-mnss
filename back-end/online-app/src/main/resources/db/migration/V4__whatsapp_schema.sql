-- V4: WhatsApp conversations and messages

CREATE TABLE whatsapp_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_phone VARCHAR(30) NOT NULL,
    customer_name VARCHAR(150),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    assigned_to UUID,
    order_id UUID REFERENCES orders(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE whatsapp_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES whatsapp_conversations(id),
    external_message_id VARCHAR(120) UNIQUE,
    direction VARCHAR(20) NOT NULL,
    sender_phone VARCHAR(30),
    sender_name VARCHAR(150),
    content TEXT,
    message_type VARCHAR(30) NOT NULL DEFAULT 'TEXT',
    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_whatsapp_conversations_phone ON whatsapp_conversations(customer_phone);
CREATE INDEX idx_whatsapp_conversations_status ON whatsapp_conversations(status);
CREATE INDEX idx_whatsapp_messages_conversation ON whatsapp_messages(conversation_id);
CREATE INDEX idx_whatsapp_messages_external_id ON whatsapp_messages(external_message_id);
