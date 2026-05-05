package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductEntity;
import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductRepository;
import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SyncInboxWorker {
    private static final Logger log = LoggerFactory.getLogger(SyncInboxWorker.class);

    private final SyncEventRepository syncEventRepository;
    private final OnlineProductRepository productRepository;

    public SyncInboxWorker(SyncEventRepository syncEventRepository, OnlineProductRepository productRepository) {
        this.syncEventRepository = syncEventRepository;
        this.productRepository = productRepository;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processInbox() {
        List<SyncEventEntity> pendingEvents = syncEventRepository.findByStatusAndDirection(
                SyncEventStatus.PENDING,
                SyncDirection.LOCAL_TO_ONLINE
        );

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Processing {} pending inbox events", pendingEvents.size());

        for (SyncEventEntity event : pendingEvents) {
            try {
                processEvent(event);
                event.markAsSynced();
            } catch (Exception e) {
                log.error("Error processing inbox event {}: {}", event.getId(), e.getMessage());
                event.markAsFailed(e.getMessage(), java.time.Instant.now().plusSeconds(300));
            }
            syncEventRepository.save(event);
        }
    }

    private void processEvent(SyncEventEntity event) {
        switch (event.getEventType()) {
            case "PRODUCT_AVAILABILITY_CHANGED":
                handleProductAvailability(event);
                break;
            case "STOCK_MOVED":
                // Just log or audit for now
                log.debug("Stock moved event received for product {}", event.getAggregateId());
                break;
            default:
                log.warn("Unknown event type for inbox: {}", event.getEventType());
        }
    }

    private void handleProductAvailability(SyncEventEntity event) {
        Map<String, Object> payload = event.getPayload();
        UUID productId = UUID.fromString((String) payload.get("productId"));
        boolean available = (boolean) payload.get("available");

        productRepository.findById(productId).ifPresentOrElse(product -> {
            product.updateAvailability(available);
            productRepository.save(product);
            log.info("Product {} availability updated to {} via sync", productId, available);
        }, () -> log.warn("Product {} not found in online database for availability update", productId));
    }
}
