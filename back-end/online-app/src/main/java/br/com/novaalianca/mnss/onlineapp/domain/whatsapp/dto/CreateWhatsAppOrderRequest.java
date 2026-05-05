package br.com.novaalianca.mnss.onlineapp.domain.whatsapp.dto;

import br.com.novaalianca.mnss.onlineapp.domain.order.DeliveryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateWhatsAppOrderRequest(
        @NotNull(message = "A conversa é obrigatória")
        UUID conversationId,

        @NotBlank(message = "O telefone do cliente é obrigatório")
        String customerPhone,

        @NotBlank(message = "O nome do cliente é obrigatório")
        String customerName,

        String customerEmail,

        @NotNull(message = "O tipo de entrega é obrigatório")
        DeliveryType deliveryType,

        @Valid
        @NotEmpty(message = "O pedido deve ter pelo menos um item")
        List<WhatsAppOrderItemRequest> items,

        String notes) {

    public record WhatsAppOrderItemRequest(
            @NotNull(message = "O produto é obrigatório")
            UUID productId,

            BigDecimal quantity,

            String observation) {}
}
