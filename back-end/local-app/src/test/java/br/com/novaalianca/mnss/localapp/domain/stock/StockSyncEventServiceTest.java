package br.com.novaalianca.mnss.localapp.domain.stock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.localapp.domain.catalog.CategoryEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StockSyncEventServiceTest {
    @Mock
    private SyncEventService syncEventService;

    private StockSyncEventService stockSyncEventService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        stockSyncEventService = new StockSyncEventService(java.util.Optional.of(syncEventService));
    }

    @Test
    void stockMovementCreatesPendingSyncEvent() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = new ProductEntity(
                new CategoryEntity("Paes"),
                "Pao frances",
                new BigDecimal("1.20"),
                UnitType.UNIT,
                PreparationSector.SEM_PREPARO);
        ReflectionTestUtils.setField(product, "id", productId);
        StockMovementEntity movement = new StockMovementEntity(
                product,
                StockMovementType.IN,
                new BigDecimal("5.000"),
                null,
                null,
                UUID.randomUUID());

        stockSyncEventService.recordStockMovementEvent(movement, new BigDecimal("5.000"));

        verify(syncEventService).createPending(
                eq("StockMovement"),
                eq(productId),
                eq("STOCK_MOVED"),
                any()
        );
    }
}
