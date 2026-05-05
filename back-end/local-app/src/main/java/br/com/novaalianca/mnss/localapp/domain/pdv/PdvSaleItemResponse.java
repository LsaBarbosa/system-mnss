package br.com.novaalianca.mnss.localapp.domain.pdv;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PdvSaleItemResponse(
        UUID id,
        UUID orderId,
        UUID productId,
        String productNameSnapshot,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        String observation,
        OrderItemStatus status,
        PreparationSector preparationSector,
        Instant createdAt,
        Instant updatedAt) {
    static PdvSaleItemResponse from(OrderItemEntity item) {
        return new PdvSaleItemResponse(
                item.getId(),
                item.getOrder().getId(),
                item.getProduct() == null ? null : item.getProduct().getId(),
                item.getProductNameSnapshot(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice(),
                item.getObservation(),
                item.getStatus(),
                item.getPreparationSector(),
                item.getCreatedAt(),
                item.getUpdatedAt());
    }
}
