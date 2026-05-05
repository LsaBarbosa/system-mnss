package br.com.novaalianca.mnss.onlineapp.domain.whatsapp.dto;

import br.com.novaalianca.mnss.onlineapp.domain.whatsapp.ConversationStatus;
import br.com.novaalianca.mnss.onlineapp.domain.whatsapp.WhatsAppConversationEntity;
import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        String customerPhone,
        String customerName,
        ConversationStatus status,
        UUID assignedTo,
        UUID orderId,
        String lastMessage,
        Instant createdAt,
        Instant updatedAt) {

    public static ConversationResponse from(WhatsAppConversationEntity entity, String lastMessage) {
        return new ConversationResponse(
                entity.getId(),
                entity.getCustomerPhone(),
                entity.getCustomerName(),
                entity.getStatus(),
                entity.getAssignedTo(),
                entity.getOrderId(),
                lastMessage,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
