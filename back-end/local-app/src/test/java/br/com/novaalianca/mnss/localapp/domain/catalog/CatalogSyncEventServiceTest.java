package br.com.novaalianca.mnss.localapp.domain.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventEntity;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventRepository;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncStatus;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CatalogSyncEventServiceTest {
    @Mock
    private SyncEventRepository syncEventRepository;

    @Test
    void availabilityChangeCreatesPendingSyncEvent() {
        when(syncEventRepository.save(any(SyncEventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
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

        service().recordAvailabilityEvent("PRODUCT_UNAVAILABLE", availability);

        ArgumentCaptor<SyncEventEntity> captor = ArgumentCaptor.forClass(SyncEventEntity.class);
        verify(syncEventRepository).save(captor.capture());
        SyncEventEntity event = captor.getValue();
        assertThat(event.getEventType()).isEqualTo("PRODUCT_UNAVAILABLE");
        assertThat(event.getAggregateId()).isEqualTo(productId);
        assertThat(event.getStatus()).isEqualTo(SyncStatus.PENDING);
    }

    private CatalogSyncEventService service() {
        return new CatalogSyncEventService(Optional.of(syncEventRepository));
    }
}
