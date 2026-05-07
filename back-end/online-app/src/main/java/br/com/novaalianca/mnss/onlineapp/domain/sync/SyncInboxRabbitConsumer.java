package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.onlineapp.config.RabbitMQConfiguration;
import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductEntity;
import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductRepository;
import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static br.com.novaalianca.mnss.onlineapp.config.RedisCacheConfiguration.*;

/**
 * Handles sync events from local-app via RabbitMQ immediately.
 * SyncInboxWorker DB polling remains active as fallback for events missed when RabbitMQ is down.
 */
@Component
public class SyncInboxRabbitConsumer {
    private static final Logger log = LoggerFactory.getLogger(SyncInboxRabbitConsumer.class);

    private final SyncEventRepository syncEventRepository;
    private final OnlineProductRepository productRepository;

    public SyncInboxRabbitConsumer(SyncEventRepository syncEventRepository,
                                   OnlineProductRepository productRepository) {
        this.syncEventRepository = syncEventRepository;
        this.productRepository = productRepository;
    }

    @RabbitListener(queues = RabbitMQConfiguration.SYNC_INBOX_QUEUE)
    @Transactional
    @CacheEvict(cacheNames = {CACHE_PUBLIC_MENU, CACHE_ONLINE_PRODUCTS}, allEntries = true)
    public void onSyncEvent(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");
        String aggregateIdStr = (String) message.get("aggregateId");

        if (eventType == null || aggregateIdStr == null) {
            log.warn("Received malformed sync message, discarding: {}", message);
            return;
        }

        log.info("Processing sync event via RabbitMQ: {} for {}", eventType, aggregateIdStr);

        switch (eventType) {
            case "PRODUCT_AVAILABLE", "PRODUCT_UNAVAILABLE" -> handleAvailabilityChange(aggregateIdStr, eventType);
            case "PRODUCT_CREATED", "PRODUCT_UPDATED", "PRODUCT_PRICE_CHANGED" -> handleProductChange(message);
            case "CATEGORY_CREATED", "CATEGORY_UPDATED" -> log.debug("Category sync event received: {}", eventType);
            default -> log.debug("Unhandled sync event type via RabbitMQ: {}", eventType);
        }
    }

    private void handleAvailabilityChange(String productIdStr, String eventType) {
        UUID productId;
        try {
            productId = UUID.fromString(productIdStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid productId in sync event: {}", productIdStr);
            return;
        }

        boolean available = "PRODUCT_AVAILABLE".equals(eventType);
        productRepository.findById(productId).ifPresentOrElse(
                product -> {
                    product.updateAvailability(available);
                    productRepository.save(product);
                    log.info("Product {} availability set to {} via RabbitMQ sync", productId, available);
                },
                () -> log.warn("Product {} not found for availability sync", productId)
        );
    }

    private void handleProductChange(Map<String, Object> message) {
        String productIdStr = (String) message.get("aggregateId");
        log.debug("Product change sync event for id: {}", productIdStr);
    }
}
