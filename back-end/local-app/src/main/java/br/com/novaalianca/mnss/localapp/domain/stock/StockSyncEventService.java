package br.com.novaalianca.mnss.localapp.domain.stock;

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

    void recordStockMovementEvent(StockMovementEntity movement, BigDecimal balanceAfter) {
        syncEventService.ifPresent(service -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("productId", movement.getProduct().getId().toString());
            payload.put("type", movement.getType().name());
            payload.put("quantity", movement.getQuantity().toPlainString());
            payload.put("balanceAfter", balanceAfter.toPlainString());
            payload.put("reason", movement.getReason());
            payload.put("orderId", movement.getOrder() == null ? null : movement.getOrder().getId().toString());

            service.createPending("StockMovement", movement.getProduct().getId(), "STOCK_MOVED", payload);
        });
    }
}
