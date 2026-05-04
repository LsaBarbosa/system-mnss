package br.com.novaalianca.mnss.localapp.domain.stock;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateStockMovementRequest(
        @NotNull UUID productId,
        @NotNull StockMovementType type,
        @NotNull @DecimalMin(value = "0.001", inclusive = true) BigDecimal quantity,
        String reason,
        UUID orderId) {}
