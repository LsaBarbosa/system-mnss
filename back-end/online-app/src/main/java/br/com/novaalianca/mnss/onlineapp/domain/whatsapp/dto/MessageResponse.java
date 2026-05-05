package br.com.novaalianca.mnss.onlineapp.domain.whatsapp.dto;

import br.com.novaalianca.mnss.onlineapp.domain.whatsapp.MessageDirection;
import br.com.novaalianca.mnss.onlineapp.domain.whatsapp.MessageStatus;
import br.com.novaalianca.mnss.onlineapp.domain.whatsapp.WhatsAppMessageEntity;
import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        MessageDirection direction,
        String senderPhone,
        String senderName,
        String content,
        String messageType,
        MessageStatus status,
        Instant createdAt) {

    public static MessageResponse from(WhatsAppMessageEntity entity) {
        return new MessageResponse(
                entity.getId(),
                entity.getDirection(),
                entity.getSenderPhone(),
                entity.getSenderName(),
                entity.getContent(),
                entity.getMessageType(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
