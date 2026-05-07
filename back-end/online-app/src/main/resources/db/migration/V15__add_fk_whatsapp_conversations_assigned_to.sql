UPDATE whatsapp_conversations
SET assigned_to = NULL
WHERE assigned_to IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM users
      WHERE users.id = whatsapp_conversations.assigned_to
  );

ALTER TABLE whatsapp_conversations
    ADD CONSTRAINT fk_whatsapp_conversations_assigned_to
    FOREIGN KEY (assigned_to)
    REFERENCES users(id);
