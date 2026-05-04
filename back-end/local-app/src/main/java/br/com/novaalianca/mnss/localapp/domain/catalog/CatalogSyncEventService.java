package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.localapp.domain.sync.SyncDirection;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEnvironment;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventEntity;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventRepository;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncStatus;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class CatalogSyncEventService {
    private final Optional<SyncEventRepository> syncEventRepository;

    CatalogSyncEventService(Optional<SyncEventRepository> syncEventRepository) {
        this.syncEventRepository = syncEventRepository;
    }

    void recordCategoryEvent(String eventType, CategoryEntity category) {
        recordEvent(eventType, "Category", category.getId(), Map.of("name", category.getName()));
    }

    void recordProductEvent(String eventType, ProductEntity product) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", product.getName());
        payload.put("price", product.getPrice().toPlainString());
        payload.put("barcode", product.getBarcode());
        recordEvent(eventType, "Product", product.getId(), payload);
    }

    void recordAvailabilityEvent(String eventType, ProductAvailabilityEntity availability) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("productId", availability.getProduct().getId().toString());
        payload.put("status", availability.getStatus().name());
        payload.put("channel", availability.getChannel().name());
        payload.put("reason", availability.getReason());
        recordEvent(eventType, "ProductAvailability", availability.getProduct().getId(), payload);
    }

    private void recordEvent(String eventType, String aggregateType, UUID aggregateId, Map<String, Object> payload) {
        SyncEventEntity event = new SyncEventEntity(
                aggregateType + ":" + aggregateId + ":" + eventType + ":" + Instant.now().toEpochMilli(),
                SyncDirection.LOCAL_TO_ONLINE,
                SyncEnvironment.LOCAL,
                SyncEnvironment.ONLINE,
                aggregateType,
                eventType,
                payload,
                SyncStatus.PENDING);
        event.assignAggregateId(aggregateId);
        syncEventRepository
                .orElseThrow(() -> new IllegalStateException("Sync event repository is not available."))
                .save(event);
    }
}
