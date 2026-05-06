package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductAvailabilityResponse(
        UUID id,
        UUID productId,
        AvailabilityStatus status,
        BigDecimal availableQuantity,
        SalesChannel channel,
        String reason,
        UUID updatedBy,
        Instant createdAt,
        Instant updatedAt,
        SyncEventStatus syncStatus) {
    static ProductAvailabilityResponse from(ProductAvailabilityEntity availability, SyncEventStatus syncStatus) {
        return new ProductAvailabilityResponse(
                availability.getId(),
                availability.getProduct().getId(),
                availability.getStatus(),
                availability.getAvailableQuantity(),
                availability.getChannel(),
                availability.getReason(),
                availability.getUpdatedBy(),
                availability.getCreatedAt(),
                availability.getUpdatedAt(),
                syncStatus);
    }
}
