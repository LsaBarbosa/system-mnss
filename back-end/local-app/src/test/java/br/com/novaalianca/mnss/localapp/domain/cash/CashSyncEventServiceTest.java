package br.com.novaalianca.mnss.localapp.domain.cash;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class CashSyncEventServiceTest {
    @Mock
    private SyncEventRepository syncEventRepository;

    @Test
    void registerOpenCreatesPendingSyncEvent() {
        when(syncEventRepository.save(any(SyncEventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CashRegisterEntity cashRegister = new CashRegisterEntity(
                UUID.randomUUID(),
                new BigDecimal("30.00"),
                CashRegisterStatus.OPEN);
        UUID registerId = UUID.randomUUID();
        ReflectionTestUtils.setField(cashRegister, "id", registerId);

        service().recordRegisterEvent("CASH_REGISTER_OPENED", cashRegister);

        ArgumentCaptor<SyncEventEntity> captor = ArgumentCaptor.forClass(SyncEventEntity.class);
        verify(syncEventRepository).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo("CASH_REGISTER_OPENED");
        assertThat(captor.getValue().getAggregateId()).isEqualTo(registerId);
        assertThat(captor.getValue().getStatus()).isEqualTo(SyncStatus.PENDING);
    }

    private CashSyncEventService service() {
        return new CashSyncEventService(Optional.of(syncEventRepository));
    }
}
