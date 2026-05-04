package br.com.novaalianca.mnss.localapp.domain.stock;

import br.com.novaalianca.mnss.localapp.domain.sync.SyncDirection;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEnvironment;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventEntity;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventRepository;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
class StockSyncEventService {
    private final Optional<SyncEventRepository> syncEventRepository;

    StockSyncEventService(Optional<SyncEventRepository> syncEventRepository) {
        this.syncEventRepository = syncEventRepository;
    }

    void recordStockMovementEvent(StockMovementEntity movement, BigDecimal balanceAfter) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("productId", movement.getProduct().getId().toString());
        payload.put("type", movement.getType().name());
        payload.put("quantity", movement.getQuantity().toPlainString());
        payload.put("balanceAfter", balanceAfter.toPlainString());
        payload.put("reason", movement.getReason());
        payload.put("orderId", movement.getOrder() == null ? null : movement.getOrder().getId().toString());

        SyncEventEntity event = new SyncEventEntity(
                "StockMovement:" + movement.getProduct().getId() + ":STOCK_MOVED:" + Instant.now().toEpochMilli(),
                SyncDirection.LOCAL_TO_ONLINE,
                SyncEnvironment.LOCAL,
                SyncEnvironment.ONLINE,
                "StockMovement",
                "STOCK_MOVED",
                payload,
                SyncStatus.PENDING);
        event.assignAggregateId(movement.getProduct().getId());
        syncEventRepository
                .orElseThrow(() -> new IllegalStateException("Sync event repository is not available."))
                .save(event);
    }
}
