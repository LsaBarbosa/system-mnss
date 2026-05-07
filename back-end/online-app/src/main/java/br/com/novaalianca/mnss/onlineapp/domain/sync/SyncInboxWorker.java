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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class SyncInboxWorker {
    private static final Logger log = LoggerFactory.getLogger(SyncInboxWorker.class);

    private static final Set<String> OUTBOUND_TYPES = Set.of("OUT", "SALE", "LOSS", "ADJUSTMENT_NEGATIVE");

    private final SyncEventRepository syncEventRepository;
    private final OnlineProductRepository productRepository;
    private final OnlineLocalSaleSummaryRepository saleSummaryRepository;
    private final OnlineStockBalanceRepository stockBalanceRepository;

    public SyncInboxWorker(SyncEventRepository syncEventRepository,
                           OnlineProductRepository productRepository,
                           OnlineLocalSaleSummaryRepository saleSummaryRepository,
                           OnlineStockBalanceRepository stockBalanceRepository) {
        this.syncEventRepository = syncEventRepository;
        this.productRepository = productRepository;
        this.saleSummaryRepository = saleSummaryRepository;
        this.stockBalanceRepository = stockBalanceRepository;
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processInbox() {
        List<SyncEventEntity> pendingEvents = syncEventRepository.findByStatusAndDirection(
                SyncEventStatus.PENDING,
                SyncDirection.LOCAL_TO_ONLINE
        );
        List<SyncEventEntity> retryingEvents = syncEventRepository.findByStatusInAndNextRetryAtBefore(
                List.of(SyncEventStatus.FAILED, SyncEventStatus.RETRYING),
                Instant.now()
        );

        List<SyncEventEntity> toProcess = new java.util.ArrayList<>(pendingEvents);
        toProcess.addAll(retryingEvents);

        if (toProcess.isEmpty()) {
            return;
        }

        log.info("Processing {} inbox events ({} pending, {} retrying)",
                toProcess.size(), pendingEvents.size(), retryingEvents.size());

        for (SyncEventEntity event : toProcess) {
            try {
                processEvent(event);
                event.markAsSynced();
            } catch (Exception e) {
                log.error("Error processing inbox event {}: {}", event.getId(), e.getMessage());
                event.markAsFailed(e.getMessage(), Instant.now().plusSeconds(300));
            }
            syncEventRepository.save(event);
        }
    }

    private void processEvent(SyncEventEntity event) {
        switch (event.getEventType()) {
            case "PRODUCT_UNAVAILABLE":
                handleProductAvailabilityByType(event, false);
                break;
            case "PRODUCT_AVAILABLE":
                handleProductAvailabilityByType(event, true);
                break;
            case "PRODUCT_AVAILABILITY_CHANGED":
                handleProductAvailability(event);
                break;
            case "STOCK_MOVED":
                handleStockMoved(event);
                break;
            case "SALE_FINISHED":
                handleSaleFinished(event);
                break;
            default:
                log.warn("Unknown event type for inbox: {}", event.getEventType());
        }
    }

    private void handleProductAvailabilityByType(SyncEventEntity event, boolean available) {
        UUID productId = event.getAggregateId();
        if (productId == null) {
            Object payloadProductId = event.getPayload().get("productId");
            if (payloadProductId == null) {
                log.warn("Cannot update product availability: no productId in event {}", event.getId());
                return;
            }
            productId = UUID.fromString(payloadProductId.toString());
        }
        final UUID resolvedId = productId;
        productRepository.findById(resolvedId).ifPresentOrElse(product -> {
            product.updateAvailability(available);
            productRepository.save(product);
            log.info("Product {} availability set to {} via {} event", resolvedId, available, event.getEventType());
        }, () -> log.warn("Product {} not found for availability update via {}", resolvedId, event.getEventType()));
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

    private void handleStockMoved(SyncEventEntity event) {
        Map<String, Object> payload = event.getPayload();
        Object productIdObj = payload.get("productId");
        if (productIdObj == null) {
            log.warn("STOCK_MOVED event {} missing productId — skipping", event.getId());
            return;
        }

        UUID productId = UUID.fromString(productIdObj.toString());
        String movementType = payload.get("type") != null ? payload.get("type").toString() : "IN";
        BigDecimal quantity = payload.get("quantity") != null
                ? new BigDecimal(payload.get("quantity").toString()) : BigDecimal.ZERO;

        BigDecimal delta = OUTBOUND_TYPES.contains(movementType) ? quantity.negate() : quantity;

        productRepository.findById(productId).ifPresentOrElse(product -> {
            OnlineStockBalanceEntity balance = stockBalanceRepository.findByProductId(productId)
                    .orElseGet(() -> new OnlineStockBalanceEntity(product));
            balance.adjust(delta);
            stockBalanceRepository.save(balance);

            if (product.isAvailable() && balance.getQuantity().signum() <= 0) {
                product.updateAvailability(false);
                productRepository.save(product);
                log.info("Product {} marked unavailable: stock zeroed via STOCK_MOVED", productId);
            }

            log.info("STOCK_MOVED applied: product={} type={} delta={} balance={}",
                    productId, movementType, delta, balance.getQuantity());
        }, () -> log.warn("Product {} not found in online database for STOCK_MOVED event {}", productId, event.getId()));
    }

    private void handleSaleFinished(SyncEventEntity event) {
        Map<String, Object> payload = event.getPayload();
        String storeId = (String) payload.get("storeId");
        UUID orderId = UUID.fromString((String) payload.get("orderId"));

        if (storeId == null || storeId.isBlank()) {
            log.warn("SALE_FINISHED event {} missing storeId — skipping", event.getId());
            return;
        }

        if (saleSummaryRepository.findByStoreIdAndLocalOrderId(storeId, orderId).isPresent()) {
            log.info("SALE_FINISHED for store={} order={} already recorded — skipping duplicate", storeId, orderId);
            return;
        }

        Long orderNumber = payload.get("orderNumber") instanceof Number n ? n.longValue() : null;
        BigDecimal totalAmount = payload.get("totalAmount") != null
                ? new BigDecimal(payload.get("totalAmount").toString()) : BigDecimal.ZERO;
        String paymentStatus = payload.get("paymentStatus") != null
                ? payload.get("paymentStatus").toString() : "UNKNOWN";
        Instant finishedAt = payload.get("finishedAt") != null
                ? Instant.parse(payload.get("finishedAt").toString()) : null;

        OnlineLocalSaleSummaryEntity summary = new OnlineLocalSaleSummaryEntity(
                storeId, orderId, orderNumber, totalAmount, paymentStatus, finishedAt, payload);
        saleSummaryRepository.save(summary);
        log.info("SALE_FINISHED recorded: store={} order={} total={}", storeId, orderId, totalAmount);
    }
}
