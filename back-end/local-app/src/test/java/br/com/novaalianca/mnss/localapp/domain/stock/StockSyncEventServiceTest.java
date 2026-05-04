package br.com.novaalianca.mnss.localapp.domain.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.localapp.domain.catalog.CategoryEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
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
class StockSyncEventServiceTest {
    @Mock
    private SyncEventRepository syncEventRepository;

    @Test
    void stockMovementCreatesPendingSyncEvent() {
        when(syncEventRepository.save(any(SyncEventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
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

        service().recordStockMovementEvent(movement, new BigDecimal("5.000"));

        ArgumentCaptor<SyncEventEntity> captor = ArgumentCaptor.forClass(SyncEventEntity.class);
        verify(syncEventRepository).save(captor.capture());
        SyncEventEntity event = captor.getValue();
        assertThat(event.getEventType()).isEqualTo("STOCK_MOVED");
        assertThat(event.getAggregateId()).isEqualTo(productId);
        assertThat(event.getStatus()).isEqualTo(SyncStatus.PENDING);
        assertThat(event.getPayload()).containsEntry("balanceAfter", "5.000");
    }

    private StockSyncEventService service() {
        return new StockSyncEventService(Optional.of(syncEventRepository));
    }
}
