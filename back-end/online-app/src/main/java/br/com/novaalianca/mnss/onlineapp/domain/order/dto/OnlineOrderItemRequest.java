package br.com.novaalianca.mnss.onlineapp.domain.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record OnlineOrderItemRequest(
        @NotNull(message = "O ID do produto é obrigatório")
        UUID productId,

        @NotNull(message = "A quantidade é obrigatória")
        @DecimalMin(value = "0.001", message = "A quantidade deve ser maior que zero")
        BigDecimal quantity,

        String observation
) {}
