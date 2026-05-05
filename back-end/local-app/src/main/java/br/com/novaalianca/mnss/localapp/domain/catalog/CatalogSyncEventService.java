package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventService;
import br.com.novaalianca.mnss.sync.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CatalogSyncEventService {
    private final Optional<SyncEventService> syncEventService;

    public CatalogSyncEventService(Optional<SyncEventService> syncEventService) {
        this.syncEventService = syncEventService;
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
        syncEventService.ifPresent(service -> service.createPending(aggregateType, aggregateId, eventType, payload));
    }
}
