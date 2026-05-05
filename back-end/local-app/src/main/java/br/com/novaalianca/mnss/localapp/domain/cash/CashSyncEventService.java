package br.com.novaalianca.mnss.localapp.domain.cash;

import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventService;
import br.com.novaalianca.mnss.sync.*;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CashSyncEventService {
    private final Optional<SyncEventService> syncEventService;

    public CashSyncEventService(Optional<SyncEventService> syncEventService) {
        this.syncEventService = syncEventService;
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
        syncEventService.ifPresent(service -> service.createPending(aggregateType, aggregateId, eventType, payload));
    }
}
