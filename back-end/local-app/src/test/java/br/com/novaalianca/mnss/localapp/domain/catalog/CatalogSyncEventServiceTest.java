package br.com.novaalianca.mnss.localapp.domain.catalog;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CatalogSyncEventServiceTest {
    @Mock
    private SyncEventService syncEventService;

    private CatalogSyncEventService catalogSyncEventService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        catalogSyncEventService = new CatalogSyncEventService(java.util.Optional.of(syncEventService));
    }

    @Test
    void availabilityChangeCreatesPendingSyncEvent() {
        ProductEntity product = new ProductEntity(
                new CategoryEntity("Paes"),
                "Pao frances",
                new BigDecimal("1.20"),
                UnitType.UNIT,
                PreparationSector.SEM_PREPARO);
        UUID productId = UUID.randomUUID();
        ReflectionTestUtils.setField(product, "id", productId);
        ProductAvailabilityEntity availability = new ProductAvailabilityEntity(
                product,
                AvailabilityStatus.UNAVAILABLE,
                SalesChannel.ALL);

        catalogSyncEventService.recordAvailabilityEvent("PRODUCT_UNAVAILABLE", availability);

        verify(syncEventService).createPending(
                eq("ProductAvailability"),
                eq(productId),
                eq("PRODUCT_UNAVAILABLE"),
                any()
        );
    }
}
