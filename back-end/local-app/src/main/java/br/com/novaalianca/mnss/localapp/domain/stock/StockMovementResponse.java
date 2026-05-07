package br.com.novaalianca.mnss.localapp.domain.stock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID productId,
        String productName,
        StockMovementType type,
        BigDecimal quantity,
        BigDecimal previousQuantity,
        BigDecimal resultingQuantity,
        String reason,
        UUID orderId,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt) {
    static StockMovementResponse from(StockMovementEntity movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getName(),
                movement.getType(),
                movement.getQuantity(),
                movement.getPreviousQuantity(),
                movement.getResultingQuantity(),
                movement.getReason(),
                movement.getOrder() == null ? null : movement.getOrder().getId(),
                movement.getCreatedBy(),
                movement.getCreatedAt(),
                movement.getUpdatedAt());
    }
}
