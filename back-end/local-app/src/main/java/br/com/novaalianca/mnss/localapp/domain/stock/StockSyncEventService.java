package br.com.novaalianca.mnss.localapp.domain.stock;

import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventService;
import br.com.novaalianca.mnss.sync.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StockSyncEventService {
    private final Optional<SyncEventService> syncEventService;

    public StockSyncEventService(Optional<SyncEventService> syncEventService) {
        this.syncEventService = syncEventService;
    }

    void recordStockMovementEvent(StockMovementEntity movement, BigDecimal resultingQuantity) {
        syncEventService.ifPresent(service -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("stockMovementId", movement.getId() != null ? movement.getId().toString() : null);
            payload.put("idempotencyKey", movement.getId() != null ? "STOCK-" + movement.getId() : null);
            payload.put("productId", movement.getProduct().getId().toString());
            payload.put("type", movement.getType().name());
            payload.put("quantity", movement.getQuantity().toPlainString());
            payload.put("previousQuantity", movement.getPreviousQuantity() != null
                    ? movement.getPreviousQuantity().toPlainString() : null);
            payload.put("resultingQuantity", resultingQuantity.toPlainString());
            payload.put("reason", movement.getReason());
            payload.put("orderId", movement.getOrder() == null ? null : movement.getOrder().getId().toString());

            service.createPending("StockMovement", movement.getProduct().getId(), "STOCK_MOVED", payload);
        });
    }

    void recordProductAvailabilityEvent(ProductEntity product) {
        syncEventService.ifPresent(service -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("productId", product.getId().toString());
            payload.put("available", product.isAvailable());

            service.createPending("Product", product.getId(), "PRODUCT_AVAILABILITY_CHANGED", payload);
        });
    }
}
