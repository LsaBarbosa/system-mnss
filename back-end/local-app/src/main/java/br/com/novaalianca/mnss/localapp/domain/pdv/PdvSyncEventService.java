package br.com.novaalianca.mnss.localapp.domain.pdv;

import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventService;
import br.com.novaalianca.mnss.sync.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PdvSyncEventService {
    private final Optional<SyncEventService> syncEventService;

    @Value("${mnss.sync.store-id:store-001}")
    private String storeId;

    public PdvSyncEventService(Optional<SyncEventService> syncEventService) {
        this.syncEventService = syncEventService;
    }

    @Transactional
    public void recordOrderFinishedEvent(OrderEntity order) {
        syncEventService.ifPresent(service -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("storeId", storeId);
            payload.put("orderId", order.getId().toString());
            payload.put("orderNumber", order.getOrderNumber());
            payload.put("totalAmount", order.getTotalAmount());
            payload.put("paymentStatus", order.getPaymentStatus().name());
            payload.put("finishedAt", order.getFinishedAt() != null
                    ? order.getFinishedAt().toString() : null);
            service.createPending("Order", order.getId(), "SALE_FINISHED", payload);
        });
    }
}
