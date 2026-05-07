package br.com.novaalianca.mnss.localapp.domain.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.localapp.domain.catalog.CategoryEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventService;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StockSyncEventServiceTest {
    @Mock private SyncEventService syncEventService;

    private StockSyncEventService stockSyncEventService;

    @BeforeEach
    void setUp() {
        stockSyncEventService = new StockSyncEventService(java.util.Optional.of(syncEventService));
    }

    @Test
    void stockMovementCreatesPendingSyncEvent() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = product(productId);
        StockMovementEntity movement = new StockMovementEntity(
                product, StockMovementType.IN, new BigDecimal("5.000"),
                null, null, UUID.randomUUID(),
                BigDecimal.ZERO, new BigDecimal("5.000"));
        UUID movementId = UUID.randomUUID();
        ReflectionTestUtils.setField(movement, "id", movementId);

        stockSyncEventService.recordStockMovementEvent(movement, new BigDecimal("5.000"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(syncEventService).createPending(
                eq("StockMovement"), eq(productId), eq("STOCK_MOVED"), payloadCaptor.capture());

        Map<String, Object> payload = payloadCaptor.getValue();
        assertThat(payload).containsEntry("productId", productId.toString());
        assertThat(payload).containsEntry("stockMovementId", movementId.toString());
        assertThat(payload).containsKey("idempotencyKey");
        assertThat(payload.get("idempotencyKey").toString()).startsWith("STOCK-");
        assertThat(payload).containsEntry("previousQuantity", "0");
        assertThat(payload).containsEntry("resultingQuantity", "5.000");
        assertThat(payload).containsEntry("type", "IN");
        assertThat(payload).containsEntry("quantity", "5.000");
    }

    private ProductEntity product(UUID id) {
        ProductEntity product = new ProductEntity(
                new CategoryEntity("Paes"),
                "Pao frances",
                new BigDecimal("1.20"),
                UnitType.UNIT,
                PreparationSector.SEM_PREPARO);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
