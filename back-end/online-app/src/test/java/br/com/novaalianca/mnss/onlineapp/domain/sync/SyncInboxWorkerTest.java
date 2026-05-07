package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductRepository;
import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncInboxWorkerTest {

    @Mock SyncEventRepository syncEventRepository;
    @Mock OnlineProductRepository productRepository;
    @Mock OnlineLocalSaleSummaryRepository saleSummaryRepository;
    @Mock OnlineStockBalanceRepository stockBalanceRepository;

    private SyncInboxWorker worker;

    @BeforeEach
    void setUp() {
        worker = new SyncInboxWorker(syncEventRepository, productRepository,
                saleSummaryRepository, stockBalanceRepository);
    }

    @Test
    void handleSaleFinished_savesNewSummary() {
        UUID orderId = UUID.randomUUID();
        String storeId = "store-001";
        SyncEventEntity event = saleFinishedEvent(storeId, orderId, "45.90", "PAID");

        when(saleSummaryRepository.existsByStoreIdAndLocalOrderId(storeId, orderId)).thenReturn(false);
        when(syncEventRepository.findByStatusAndDirection(any(), any())).thenReturn(List.of(event));
        when(syncEventRepository.findByStatusInAndNextRetryAtBefore(any(), any())).thenReturn(List.of());

        worker.processInbox();

        ArgumentCaptor<OnlineLocalSaleSummaryEntity> captor =
                ArgumentCaptor.forClass(OnlineLocalSaleSummaryEntity.class);
        verify(saleSummaryRepository).save(captor.capture());

        OnlineLocalSaleSummaryEntity saved = captor.getValue();
        assertThat(saved.getStoreId()).isEqualTo(storeId);
        assertThat(saved.getLocalOrderId()).isEqualTo(orderId);
        assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("45.90"));
        assertThat(saved.getPaymentStatus()).isEqualTo("PAID");
    }

    @Test
    void handleSaleFinished_skipsDuplicateIdempotently() {
        UUID orderId = UUID.randomUUID();
        String storeId = "store-001";
        SyncEventEntity event = saleFinishedEvent(storeId, orderId, "20.00", "PAID");

        when(saleSummaryRepository.existsByStoreIdAndLocalOrderId(storeId, orderId)).thenReturn(true);
        when(syncEventRepository.findByStatusAndDirection(any(), any())).thenReturn(List.of(event));
        when(syncEventRepository.findByStatusInAndNextRetryAtBefore(any(), any())).thenReturn(List.of());

        worker.processInbox();

        verify(saleSummaryRepository, never()).save(any());
    }

    @Test
    void processInbox_reprocessesFailedEventsWithExpiredRetry() {
        UUID orderId = UUID.randomUUID();
        String storeId = "store-002";
        SyncEventEntity event = saleFinishedEvent(storeId, orderId, "30.00", "PAID");

        when(saleSummaryRepository.existsByStoreIdAndLocalOrderId(storeId, orderId)).thenReturn(false);
        when(syncEventRepository.findByStatusAndDirection(any(), any())).thenReturn(List.of());
        when(syncEventRepository.findByStatusInAndNextRetryAtBefore(any(), any())).thenReturn(List.of(event));

        worker.processInbox();

        verify(saleSummaryRepository).save(any(OnlineLocalSaleSummaryEntity.class));
    }

    @Test
    void processInbox_reprocessesRetryingEventsWithExpiredRetry() {
        UUID orderId = UUID.randomUUID();
        String storeId = "store-003";
        SyncEventEntity event = saleFinishedEvent(storeId, orderId, "15.00", "PENDING");

        when(saleSummaryRepository.existsByStoreIdAndLocalOrderId(storeId, orderId)).thenReturn(false);
        when(syncEventRepository.findByStatusAndDirection(any(), any())).thenReturn(List.of());
        when(syncEventRepository.findByStatusInAndNextRetryAtBefore(any(), any())).thenReturn(List.of(event));

        worker.processInbox();

        verify(saleSummaryRepository).save(any(OnlineLocalSaleSummaryEntity.class));
    }

    @Test
    void handleSaleFinished_skipsMissingStoreId() {
        UUID orderId = UUID.randomUUID();
        SyncEventEntity event = saleFinishedEvent(null, orderId, "10.00", "PENDING");

        when(syncEventRepository.findByStatusAndDirection(any(), any())).thenReturn(List.of(event));
        when(syncEventRepository.findByStatusInAndNextRetryAtBefore(any(), any())).thenReturn(List.of());

        worker.processInbox();

        verify(saleSummaryRepository, never()).existsByStoreIdAndLocalOrderId(any(), any());
        verify(saleSummaryRepository, never()).save(any());
    }

    @Test
    void handleStockMoved_adjustsOnlineBalanceForInboundMovement() {
        UUID productId = UUID.randomUUID();
        SyncEventEntity event = stockMovedEvent(productId, "IN", "10.000");

        br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductEntity product =
                mock(br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductEntity.class);
        when(product.isAvailable()).thenReturn(true);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductId(productId)).thenReturn(Optional.empty());
        when(stockBalanceRepository.save(any(OnlineStockBalanceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(syncEventRepository.findByStatusAndDirection(any(), any())).thenReturn(List.of(event));
        when(syncEventRepository.findByStatusInAndNextRetryAtBefore(any(), any())).thenReturn(List.of());

        worker.processInbox();

        ArgumentCaptor<OnlineStockBalanceEntity> captor =
                ArgumentCaptor.forClass(OnlineStockBalanceEntity.class);
        verify(stockBalanceRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualByComparingTo("10.000");
    }

    @Test
    void handleStockMoved_adjustsOnlineBalanceForSaleMovement() {
        UUID productId = UUID.randomUUID();
        SyncEventEntity event = stockMovedEvent(productId, "SALE", "3.000");

        br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductEntity product =
                mock(br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductEntity.class);
        when(product.isAvailable()).thenReturn(true);

        OnlineStockBalanceEntity existingBalance =
                new OnlineStockBalanceEntity(product);
        existingBalance.adjust(new BigDecimal("5.000"));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductId(productId)).thenReturn(Optional.of(existingBalance));
        when(stockBalanceRepository.save(any(OnlineStockBalanceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(syncEventRepository.findByStatusAndDirection(any(), any())).thenReturn(List.of(event));
        when(syncEventRepository.findByStatusInAndNextRetryAtBefore(any(), any())).thenReturn(List.of());

        worker.processInbox();

        assertThat(existingBalance.getQuantity()).isEqualByComparingTo("2.000");
    }

    @Test
    void handleStockMoved_skipsWhenProductNotFound() {
        UUID productId = UUID.randomUUID();
        SyncEventEntity event = stockMovedEvent(productId, "IN", "5.000");

        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        when(syncEventRepository.findByStatusAndDirection(any(), any())).thenReturn(List.of(event));
        when(syncEventRepository.findByStatusInAndNextRetryAtBefore(any(), any())).thenReturn(List.of());

        worker.processInbox();

        verify(stockBalanceRepository, never()).save(any());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private SyncEventEntity saleFinishedEvent(String storeId, UUID orderId,
                                               String totalAmount, String paymentStatus) {
        Map<String, Object> payload = new HashMap<>();
        if (storeId != null) payload.put("storeId", storeId);
        payload.put("orderId", orderId.toString());
        payload.put("orderNumber", 42L);
        payload.put("totalAmount", totalAmount);
        payload.put("paymentStatus", paymentStatus);
        payload.put("finishedAt", Instant.now().toString());

        return new SyncEventEntity(
                UUID.randomUUID().toString(),
                SyncDirection.LOCAL_TO_ONLINE,
                br.com.novaalianca.mnss.sync.SyncEnvironment.LOCAL,
                br.com.novaalianca.mnss.sync.SyncEnvironment.ONLINE,
                "Order",
                "SALE_FINISHED",
                payload,
                SyncEventStatus.PENDING
        );
    }

    private SyncEventEntity stockMovedEvent(UUID productId, String type, String quantity) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", productId.toString());
        payload.put("type", type);
        payload.put("quantity", quantity);
        payload.put("previousQuantity", "0");
        payload.put("resultingQuantity", quantity);
        payload.put("stockMovementId", UUID.randomUUID().toString());
        payload.put("idempotencyKey", "STOCK-" + UUID.randomUUID());

        return new SyncEventEntity(
                UUID.randomUUID().toString(),
                SyncDirection.LOCAL_TO_ONLINE,
                br.com.novaalianca.mnss.sync.SyncEnvironment.LOCAL,
                br.com.novaalianca.mnss.sync.SyncEnvironment.ONLINE,
                "StockMovement",
                "STOCK_MOVED",
                payload,
                SyncEventStatus.PENDING
        );
    }
}
