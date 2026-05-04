package br.com.novaalianca.mnss.localapp.domain.cash;

import br.com.novaalianca.mnss.localapp.domain.sync.SyncDirection;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEnvironment;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventEntity;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventRepository;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncStatus;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
class CashSyncEventService {
    private final Optional<SyncEventRepository> syncEventRepository;

    CashSyncEventService(Optional<SyncEventRepository> syncEventRepository) {
        this.syncEventRepository = syncEventRepository;
    }

    void recordRegisterEvent(String eventType, CashRegisterEntity cashRegister) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("operatorId", cashRegister.getOperatorId().toString());
        payload.put("status", cashRegister.getStatus().name());
        payload.put("openingAmount", cashRegister.getOpeningAmount().toPlainString());
        payload.put("closingAmount", cashRegister.getClosingAmount() == null ? null : cashRegister.getClosingAmount().toPlainString());
        recordEvent(eventType, "CashRegister", cashRegister.getId(), payload);
    }

    void recordMovementEvent(CashMovementEntity movement) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("cashRegisterId", movement.getCashRegister().getId().toString());
        payload.put("type", movement.getType().name());
        payload.put("paymentMethod", movement.getPaymentMethod() == null ? null : movement.getPaymentMethod().name());
        payload.put("amount", movement.getAmount().toPlainString());
        payload.put("description", movement.getDescription());
        recordEvent("CASH_MOVEMENT_CREATED", "CashMovement", movement.getCashRegister().getId(), payload);
    }

    private void recordEvent(
            String eventType,
            String aggregateType,
            java.util.UUID aggregateId,
            Map<String, Object> payload) {
        SyncEventEntity event = new SyncEventEntity(
                aggregateType + ":" + aggregateId + ":" + eventType + ":" + Instant.now().toEpochMilli(),
                SyncDirection.LOCAL_TO_ONLINE,
                SyncEnvironment.LOCAL,
                SyncEnvironment.ONLINE,
                aggregateType,
                eventType,
                payload,
                SyncStatus.PENDING);
        event.assignAggregateId(aggregateId);
        syncEventRepository
                .orElseThrow(() -> new IllegalStateException("Sync event repository is not available."))
                .save(event);
    }
}
