package br.com.novaalianca.mnss.localapp.domain.cash;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CashSyncEventServiceTest {
    @Mock
    private SyncEventService syncEventService;

    private CashSyncEventService cashSyncEventService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        cashSyncEventService = new CashSyncEventService(java.util.Optional.of(syncEventService));
    }

    @Test
    void registerOpenCreatesPendingSyncEvent() {
        CashRegisterEntity cashRegister = new CashRegisterEntity(
                UUID.randomUUID(),
                new BigDecimal("30.00"),
                CashRegisterStatus.OPEN);
        UUID registerId = UUID.randomUUID();
        ReflectionTestUtils.setField(cashRegister, "id", registerId);

        cashSyncEventService.recordRegisterEvent("CASH_REGISTER_OPENED", cashRegister);

        verify(syncEventService).createPending(
                eq("CashRegister"),
                eq(registerId),
                eq("CASH_REGISTER_OPENED"),
                any()
        );
    }
}
